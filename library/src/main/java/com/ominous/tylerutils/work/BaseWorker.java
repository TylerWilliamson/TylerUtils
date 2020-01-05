package com.ominous.tylerutils.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public abstract class BaseWorker<T extends GenericWorker> extends Worker implements GenericWorker.WorkerFactory<T> {
    public static final String KEY_ERROR_MESSAGE = "key_error_message", KEY_STACK_TRACE = "key_stack_trace";
    private T worker;

    public BaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        setWorker(getWorker(context));
    }

    public void setWorker(T worker) {
        this.worker = worker;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (worker == null) {
                return Result.failure(new Data.Builder().putString(KEY_ERROR_MESSAGE,"GenericWorker is null").build());
            } else {
                return Result.success(worker.doWork(new GenericWorker.WorkerInterface() {
                    @Override
                    public boolean isCancelled() {
                        return BaseWorker.this.isStopped();
                    }

                    @Override
                    public void onProgress(int progress, int max) {
                        //Cannot post progress from a Worker
                    }
                }).getData());
            }
        } catch (Throwable t) {
            StringBuilder stackTrace = new StringBuilder();

            for (StackTraceElement ste : t.getStackTrace()) {
                stackTrace.append(ste.toString()).append('\n');
            }

            return Result.failure(new Data.Builder().putString(KEY_ERROR_MESSAGE,t.getMessage()).putString(KEY_STACK_TRACE,stackTrace.toString()).build());
        }
    }
}
