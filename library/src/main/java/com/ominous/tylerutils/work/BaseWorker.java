package com.ominous.tylerutils.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public abstract class BaseWorker<T extends GenericWorker> extends Worker implements GenericWorker.WorkerFactory<T> {
    private T worker;

    public BaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        worker = getWorker(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        return worker.doWork(new GenericWorker.WorkerInterface() {
            @Override
            public boolean isCancelled() {
                return BaseWorker.this.isStopped();
            }

            @Override
            public void onProgress(int progress, int max) {
                //Cannot post progress from a Worker
            }
        });
    }
}
