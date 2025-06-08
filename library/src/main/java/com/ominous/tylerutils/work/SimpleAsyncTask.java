/*
 * Copyright 2020 - 2025 Tyler Williamson
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

package com.ominous.tylerutils.work;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class SimpleAsyncTask<T, V> implements ICancelableTask {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Future<?> taskFuture;

    @SuppressWarnings("unchecked") //This is a "Simple" AsyncTask
    abstract protected V doInBackground(T... inputs);

    protected void onPostExecute(V output) {
    }

    protected void onProgressUpdate(int progress, int max) {
    }

    @SuppressWarnings("unchecked")
    public final void execute(T... inputs) {
        if (taskFuture == null) {
            taskFuture = executorService.submit(() -> {
                final V result = doInBackground(inputs);
                handler.post(() -> onPostExecute(result));
            });
        } else {
            throw new IllegalStateException("Task already started");
        }
    }

    public final void postProgress(int progress, int max) {
        handler.post(() -> onProgressUpdate(progress, max));
    }

    public final boolean cancel(boolean mayInterruptIfRunning) {
        return taskFuture != null && taskFuture.cancel(mayInterruptIfRunning);
    }

    public final boolean isCancelled() {
        return taskFuture != null && taskFuture.isCancelled();
    }
}
