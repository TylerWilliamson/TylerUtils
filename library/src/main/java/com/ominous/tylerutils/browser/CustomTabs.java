/*
 *     Copyright 2020 - 2021 Tyler Williamson
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

package com.ominous.tylerutils.browser;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsService;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.content.ContextCompat;

import com.ominous.tylerutils.R;
import com.ominous.tylerutils.util.BitmapUtils;
import com.ominous.tylerutils.util.ColorUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CustomTabs {
    private static CustomTabs instance = null;
    private static CustomTabsServiceConnection customTabsServiceConnection = new CustomTabsServiceConnection() {
        @Override
        public void onCustomTabsServiceConnected(@NonNull ComponentName name, @NonNull CustomTabsClient client) {
            instance.onClientObtained(client);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            instance.client = null;
        }
    };

    private WeakReference<Context> context;
    private CustomTabsClient client = null;
    private CustomTabsSession session = null;
    private String customTabsPackageName;
    private long lastLaunch = 0;
    private CustomTabsIntent customTabsIntent;

    private List<ResolveInfo> browserInfoList;

    private ArrayList<Uri> likelyUris = new ArrayList<>();

    private CustomTabs(Context context) {
        this.context = new WeakReference<>(context);

        ArrayList<ResolveInfo> packages = getCustomTabsPackages(context);

        this.customTabsPackageName = packages == null || packages.size() == 0 ? null : packages.get(0).activityInfo.packageName;

        this.bind(context);

        this.setColor(0);
    }

    public static CustomTabs getInstance(Context context, Uri... likelyUris) {
        if (instance == null) {
            instance = new CustomTabs(context.getApplicationContext());
        }

        instance.addLikelyUris(likelyUris);

        return instance;
    }

    private void bind(Context context) {
        if (customTabsPackageName != null) {
            CustomTabsClient.bindCustomTabsService(context, customTabsPackageName, customTabsServiceConnection);
        }
    }

    @Override
    public void finalize() throws Throwable {
        if (customTabsServiceConnection != null) {
            context.get().unbindService(customTabsServiceConnection);
            customTabsServiceConnection = null;
        }

        super.finalize();
    }

    private void onClientObtained(CustomTabsClient client) {
        this.client = client;

        client.warmup(0);

        session = client.newSession(null);

        warmUpLikelyUris();
    }

    public void setColor(int color) {
        this.customTabsIntent = new CustomTabsIntent.Builder(session)
                .addDefaultShareMenuItem()
                .enableUrlBarHiding()
                .setCloseButtonIcon(getBackArrow(context.get(), ColorUtils.isColorBright(color) ? 0xFF000000 : 0xFFFFFFFF))
                .setToolbarColor(color)
                .setShowTitle(true)
                .setStartAnimations(context.get(), R.anim.slide_right_in, R.anim.slide_left_out)
                .setExitAnimations(context.get(), R.anim.slide_left_in, R.anim.slide_right_out)
                .build();
    }

    public void addLikelyUris(Uri... uris) {
        likelyUris.addAll(Arrays.asList(uris));

        if (client != null) {
            warmUpLikelyUris();
        }
    }

    private void warmUpLikelyUris() {
        if (session != null && likelyUris.size() > 0) {

            ArrayList<Bundle> otherLikelyBundles = new ArrayList<>();

            for (int i = 1, l = likelyUris.size(); i < l; i++) {
                Bundle likelyUrl = new Bundle();

                likelyUrl.putParcelable(CustomTabsService.KEY_URL, likelyUris.get(i));

                otherLikelyBundles.add(likelyUrl);
            }

            session.mayLaunchUrl(likelyUris.get(0), null, otherLikelyBundles);
        }
    }

    public void launch(Context context, Uri uri) {
        long currentTime = Calendar.getInstance().getTimeInMillis();

        if (currentTime - lastLaunch > 300) { //rate limit
            lastLaunch = currentTime;


            //Try to use native app
            boolean launched = Build.VERSION.SDK_INT >= 30 ?
                    launchNative(context, uri) :
                    launchNativeLegacy(context, uri);

            //Otherwise, try a Custom Tab
            if (!launched) {
                customTabsIntent.launchUrl(context, uri);
            }
        }
    }

    private boolean launchNative(Context context, Uri uri) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER));
            return true;
        } catch (ActivityNotFoundException ex) {
            return false;
        }
    }

    private boolean launchNativeLegacy(Context context, Uri uri) {
        PackageManager pm = context.getPackageManager();

        if (browserInfoList == null) {
            browserInfoList = pm.queryIntentActivities(
                    new Intent()
                            .setAction(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.fromParts("http", "", null)), 0);
        }

        Intent uriIntent = new Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE);

        boolean appIsBrowser;
        for (ResolveInfo uriResolveInfo : pm.queryIntentActivities(uriIntent, 0)) {
            appIsBrowser = false;
            for (ResolveInfo browserResolveInfo : browserInfoList) {
                if (uriResolveInfo.resolvePackageName.equals(browserResolveInfo.resolvePackageName)) {
                    appIsBrowser = true;
                    break;
                }
            }

            if (!appIsBrowser) {
                // We found native handlers. Launch the Intent.
                context.startActivity(uriIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            }
        }

        return false;
    }

    private static ArrayList<ResolveInfo> getCustomTabsPackages(Context context) {
        PackageManager packageManager = context.getPackageManager();

        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TylerWilliamson/TylerUtils/")),
                customTabsIntent = new Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);

        ArrayList<ResolveInfo> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : packageManager.queryIntentActivities(activityIntent, 0)) {
            if (packageManager.resolveService(customTabsIntent.setPackage(info.activityInfo.packageName), 0) != null) {
                packagesSupportingCustomTabs.add(info);
            }
        }

        return packagesSupportingCustomTabs;
    }

    private static Bitmap getBackArrow(Context context, int color) {
        return BitmapUtils.drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_white_24dp), color);
    }
}
