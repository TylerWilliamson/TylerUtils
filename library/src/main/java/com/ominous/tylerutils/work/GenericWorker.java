package com.ominous.tylerutils.work;

import android.content.Context;

import androidx.work.ListenableWorker;

public abstract class GenericWorker {
    private Context context;

    public GenericWorker(Context context) {
        this.context = context;
    }

    public abstract ListenableWorker.Result doWork(WorkerInterface workerInterface);

    public interface WorkerInterface {
        boolean isCancelled();
        void onProgress(int progress, int max);
    }

    public interface WorkerFactory <T extends GenericWorker> {
        T getWorker(Context context);
    }

    public Context getContext() {
        return context;
    }
}
