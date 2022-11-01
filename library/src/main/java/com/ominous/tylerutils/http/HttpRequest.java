/*
 *     Copyright 2020 - 2022 Tyler Williamson
 *
 *     This file is part of TylerUtils.
 *
 *     TylerUtils is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     TylerUtils is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.ominous.tylerutils.async.Promise;
import com.ominous.tylerutils.work.GenericResults;
import com.ominous.tylerutils.work.SimpleAsyncTask;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import androidx.work.Data;

public class HttpRequest {
    public static final String METHOD_GET = "GET", METHOD_POST = "POST", COMPRESSION_NONE = "none", COMPRESSION_GZIP = "gzip";
    private static final String UTF8 = "UTF-8";
    @SuppressWarnings("CharsetObjectCanBeUsed")
    private static final Charset UTF8Charset = Build.VERSION.SDK_INT >= 19 ? StandardCharsets.UTF_8 : Charset.forName(UTF8);
    private final String url;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private final Map<String, String> bodyParams = new HashMap<>();
    private String method = METHOD_GET;
    private String compression = COMPRESSION_NONE;
    private Map<String, List<String>> responseHeaders;
    private HttpURLConnection conn;

    public HttpRequest(String url) {
        this.url = url;
    }

    public HttpRequest addHeader(String name, String value) {
        requestHeaders.put(name, value);

        return this;
    }

    public HttpRequest addBodyParam(String name, String value) {
        bodyParams.put(name, value);

        return this;
    }

    public HttpRequest setMethod(String method) {
        this.method = method;

        return this;
    }

    public HttpRequest setCompression(String compressionType) {
        this.compression = compressionType;

        return this;
    }

    public InputStream connect() throws HttpException {
        //This handles HTTPS too
        try {
            conn = (HttpURLConnection) new URL(this.url).openConnection();
            conn.setRequestMethod(this.method);

            for (HashMap.Entry<String, String> entry : this.requestHeaders.entrySet()) {
                conn.addRequestProperty(entry.getKey(), entry.getValue());
            }

            if (this.method.equals(METHOD_POST) && this.bodyParams.size() > 0) {
                StringBuilder body = new StringBuilder();

                for (HashMap.Entry<String, String> entry : this.bodyParams.entrySet()) {
                    if (body.length() > 0) {
                        body.append('&');
                    }

                    body.append(URLEncoder.encode(entry.getKey(), UTF8))
                            .append('=')
                            .append(URLEncoder.encode(entry.getValue(), UTF8));
                }

                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8Charset))) {
                    writer.write(body.toString());
                    writer.flush();
                }
            }

            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                responseHeaders = conn.getHeaderFields();

                return compression.equals(COMPRESSION_GZIP) ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream();
            } else {
                throw new HttpException("HTTP Error: " + conn.getResponseCode() + " " + conn.getResponseMessage());
            }
        } catch (MalformedURLException e) {
            throw new HttpException("HTTP: Bad URL: " + url, e);
        } catch (ProtocolException e) {
            throw new HttpException("HTTP: Bad Method: " + this.method, e);
        } catch (IOException e) {
            throw new HttpException("HTTP: I/O Error: " + e.getMessage(), e);
        }
    }

    private void disconnect() {
        if (conn != null) {
            conn.disconnect();
        }
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public String fetch() throws HttpException, IOException {
        try (InputStream inputStream = connect();
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024]; // 1MB
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString(UTF8);
        } finally {
            this.disconnect();
        }
    }

    public Bitmap fetchBitmap() throws HttpException, IOException {
        try (InputStream inputStream = connect()) {
            return BitmapFactory.decodeStream(inputStream);
        } finally {
            this.disconnect();
        }
    }

    public Promise<Void, String> fetchAsync() {
        return Promise.create((a) -> {
            return fetch();
        });
    }

    public Promise<Void, Bitmap> fetchBitmapAsync() {
        return Promise.create((a) -> {
            return fetchBitmap();
        });
    }

    /**
     * @Deprecated - Use Promise fetchAsync() instead
     */
    public void fetchAsync(RequestListener listener) {
        new RequestTask(this, listener).execute();
    }

    /**
     * @Deprecated - Use Promise fetchBitmapAsync() instead
     */
    public void fetchBitmapAsync(BitmapRequestListener listener) {
        new RequestTask(this, listener).execute();
    }

    private interface GenericRequestListener {
        void onRequestFailure(Exception error);
    }

    public interface RequestListener extends GenericRequestListener {
        void onRequestSuccess(String result);
    }

    public interface BitmapRequestListener extends GenericRequestListener {
        void onRequestSuccess(Bitmap result);
    }

    private static class HttpResults extends GenericResults<Object> {
        public HttpResults(Data data, Object results) {
            super(data, results);
        }
    }

    private static class RequestTask extends SimpleAsyncTask<HttpRequest, HttpResults> {
        private final HttpRequest httpRequest;
        private final GenericRequestListener requestListener;
        private Exception error;

        RequestTask(HttpRequest httpRequest, GenericRequestListener requestListener) {
            this.httpRequest = httpRequest;
            this.requestListener = requestListener;
        }

        @Override
        protected HttpResults doInBackground(HttpRequest... inputs) {
            try {
                if (requestListener instanceof RequestListener) {
                    return new HttpResults(Data.EMPTY, httpRequest.fetch());
                } else if (requestListener instanceof BitmapRequestListener) {
                    return new HttpResults(Data.EMPTY, httpRequest.fetchBitmap());
                } else {
                    error = new IllegalArgumentException("Invalid request type");
                    return null;
                }
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(HttpResults result) {
            if (error == null) {
                if (requestListener instanceof RequestListener) {
                    ((RequestListener) requestListener).onRequestSuccess((String) result.getResults());
                } else if (requestListener instanceof BitmapRequestListener) {
                    ((BitmapRequestListener) requestListener).onRequestSuccess((Bitmap) result.getResults());
                } else {
                    throw new IllegalArgumentException("Invalid request type");
                }
            } else {
                requestListener.onRequestFailure(error);
            }
        }
    }
}
