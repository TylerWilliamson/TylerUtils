package com.ominous.tylerutils.work;

import android.content.Context;

import androidx.annotation.Nullable;

public abstract class GenericWorker<T extends GenericResults> {
    private Context context;

    public GenericWorker(Context context) {
        this.context = context;
    }

    public abstract T doWork(WorkerInterface workerInterface) throws Throwable;

    public interface WorkerInterface {
        boolean isCancelled();
        void onProgress(int progress, int max);
    }

    public interface WorkerFactory <T extends GenericWorker> {
        @Nullable
        T getWorker(Context context);
    }

    public Context getContext() {
        return context;
    }
}
