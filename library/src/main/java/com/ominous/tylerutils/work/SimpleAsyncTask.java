package com.ominous.tylerutils.work;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class SimpleAsyncTask<T,V> {
    private Future<?> taskFuture;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @SuppressWarnings("unchecked") //This is a "Simple" AsyncTask
    abstract protected V doInBackground(T... inputs);
    protected void onPostExecute(V output) {}

    public void execute(T... inputs) {
        if (taskFuture == null) {
            taskFuture = executorService.submit(() -> {
                final V result = doInBackground(inputs);
                handler.post(() -> onPostExecute(result));
            });
        } else {
            throw new IllegalStateException("Task already started");
        }
    }
}
