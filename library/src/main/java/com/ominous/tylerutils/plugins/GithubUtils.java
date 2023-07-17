/*
 * Copyright 2020 - 2023 Tyler Williamson
 *
 * This file is part of TylerUtils.
 *
 * TylerUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TylerUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.plugins;

import com.ominous.tylerutils.http.HttpRequest;
import com.ominous.tylerutils.util.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import androidx.annotation.Nullable;

public class GithubUtils {
    private final static String githubApiUri = "https://api.github.com/repos/%1$s/%2$s/%3$s";
    private final static String githubUri = "https://github.com/%1$s/%2$s/%3$s";

    public static GitHubRepo getRepo(String user, String repo) {
        return new GitHubRepo(user, repo);
    }

    public static class GitHubRepo {
        private final String user;
        private final String repo;

        private GitHubRepo(String user, String repo) {
            this.user = user;
            this.repo = repo;
        }

        public GitHubRelease getRelease(String tag) throws GithubException {
            try {
                return JsonUtils.deserialize(
                        GitHubRelease.class,
                        new JSONObject(
                                new HttpRequest(String.format(githubApiUri, user, repo, "releases/tags/" + tag))
                                        .fetchAsync()
                                        .await()
                        )
                );
            } catch (InterruptedException | ExecutionException e) { //InterruptedException, ExecutionException
                throw new GithubException("Process Interrupted", e);
            } catch (IllegalAccessException e) {
                //will not happen
            } catch (InstantiationException e) {
                //will not happen
            } catch (JSONException e) {
                throw new GithubException("Malformed JSON Received", e);
            }

            return null;
        }

        public GitHubRelease getLatestRelease() throws GithubException {
            try {
                return JsonUtils.deserialize(
                        GitHubRelease.class,
                        new JSONObject(
                                new HttpRequest(String.format(githubApiUri, user, repo, "releases/latest"))
                                        .fetchAsync()
                                        .await()
                        )
                );
            } catch (InterruptedException | ExecutionException e) { //InterruptedException, ExecutionException
                throw new GithubException("Process Interrupted", e);
            } catch (IllegalAccessException e) {
                //will not happen
            } catch (InstantiationException e) {
                //will not happen
            } catch (JSONException e) {
                throw new GithubException("Malformed JSON Received", e);
            }

            return null;
        }

        public String getNewIssueUrl(@Nullable String body, @Nullable String title) {

            StringBuilder newIssueStringBuilder = new StringBuilder(String.format(githubUri, user, repo, "issues/new"));

            try {
                if (body != null || title != null) {
                    newIssueStringBuilder.append('?');

                    if (body != null) {
                        newIssueStringBuilder.append("body=").append(URLEncoder.encode(body, "UTF-8"));

                        if (title != null) {
                            newIssueStringBuilder.append('&');
                        }
                    }

                    if (title != null) {
                        newIssueStringBuilder.append("title=").append(URLEncoder.encode(title, "UTF-8"));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return newIssueStringBuilder.toString();
        }
    }

    public static class GitHubRelease {
        public String tag_name;
        public String body;
        public boolean draft;
        public String published_at;
    }

    public static class GithubException extends Exception {
        public GithubException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
