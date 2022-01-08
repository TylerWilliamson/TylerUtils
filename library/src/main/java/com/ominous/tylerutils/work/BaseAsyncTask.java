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

package com.ominous.tylerutils.work;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class BaseAsyncTask<T extends GenericWorker<?>> implements GenericWorker.WorkerFactory<T>, ICancelableTask  {
    private WeakReference<T> workerRef;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> taskFuture;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public BaseAsyncTask(Context context) {
        setWorker(getWorker(context));
    }

    public final void setWorker(T worker) {
        workerRef = new WeakReference<>(worker);
    }

    public final GenericResults<?> doWork() throws Throwable {
        GenericWorker<?> worker = workerRef.get();

        if (worker == null) {
            throw new Exception("GenericWorker is null");
        }

        return worker.doWork(new GenericWorker.WorkerInterface() {
            @Override
            public boolean isCancelled() {
                return taskFuture != null && taskFuture.isCancelled();
            }

            @Override
            public void onProgress(int progress, int max) {
                handler.post(() -> onProgressUpdate(progress, max));
            }
        });
    }

    public final boolean cancel(boolean mayInterruptIfRunning) {
        return taskFuture != null && taskFuture.cancel(mayInterruptIfRunning);
    }

    protected abstract GenericResults<?> doInBackground(Void... voids);
    protected void onPostExecute(GenericResults<?> result) {}
    protected void onProgressUpdate(int progress, int max) {}

    public final void execute() {
        if (taskFuture == null) {
            taskFuture = executorService.submit(() -> {
                final GenericResults<?> result = doInBackground();
                handler.post(() -> onPostExecute(result));
            });
        } else {
            throw new IllegalStateException("Task already started");
        }
    }
}
