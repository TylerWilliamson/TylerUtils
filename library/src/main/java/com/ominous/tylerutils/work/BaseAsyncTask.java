package com.ominous.tylerutils.work;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("rawtypes")
public abstract class BaseAsyncTask<T extends GenericWorker> implements GenericWorker.WorkerFactory<T>  {
    private WeakReference<T> workerRef;
    private boolean isCancelled;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // change according to your requirements
    private Future taskFuture;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public BaseAsyncTask(Context context) {
        setWorker(getWorker(context));
    }

    public void setWorker(T worker) {
        workerRef = new WeakReference<>(worker);
    }

    public final GenericResults doWork() throws Throwable {
        GenericWorker worker = workerRef.get();

        if (worker == null) {
            throw new Exception("GenericWorker is null");
        }

        return worker.doWork(new GenericWorker.WorkerInterface() {
            @Override
            public boolean isCancelled() {
                return BaseAsyncTask.this.isCancelled;
            }

            @Override
            public void onProgress(int progress, int max) {
                //BaseAsyncTask.this.publishProgress(progress,max);
            }
        });
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        isCancelled = true;

        return taskFuture.cancel(mayInterruptIfRunning);
    }

    protected abstract GenericResults doInBackground(Void... voids);
    protected void onPostExecute(GenericResults result) {}

    public void execute() {
        if (taskFuture == null) {
            taskFuture = executorService.submit(() -> {
                final GenericResults result = doInBackground();
                handler.post(() -> onPostExecute(result));
            });
        } else {
            throw new IllegalStateException("Task already started");
        }
    }
}
