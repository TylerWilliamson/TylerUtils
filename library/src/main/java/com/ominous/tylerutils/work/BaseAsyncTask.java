package com.ominous.tylerutils.work;

import android.content.Context;
import android.os.AsyncTask;

import androidx.work.Data;

import java.lang.ref.WeakReference;

public abstract class BaseAsyncTask<T extends GenericWorker> extends AsyncTask<Void,Integer, Data> implements GenericWorker.WorkerFactory<T>  {
    private WeakReference<T> workerRef;

    public BaseAsyncTask(Context context) {
        workerRef = new WeakReference<>(getWorker(context));
    }

    public final Data doWork() throws Throwable {
        return workerRef.get().doWork(new GenericWorker.WorkerInterface() {
            @Override
            public boolean isCancelled() {
                return BaseAsyncTask.this.isCancelled();
            }

            @Override
            public void onProgress(int progress, int max) {
                BaseAsyncTask.this.publishProgress(progress,max);
            }
        });
    }
}
