package com.ominous.tylerutils.work;

import android.content.Context;
import android.os.AsyncTask;

import androidx.work.Data;

import java.lang.ref.WeakReference;

public abstract class BaseAsyncTask<T extends GenericWorker> extends AsyncTask<Void,Integer, Data> implements GenericWorker.WorkerFactory<T>  {
    private WeakReference<T> workerRef;

    public BaseAsyncTask(Context context) {
        setWorker(getWorker(context));
    }

    public void setWorker(T worker) {
        workerRef = new WeakReference<>(worker);
    }

    public final Object doWork() throws Throwable {
        GenericWorker worker = workerRef.get();

        if (worker == null) {
            throw new Exception("GenericWorker is null");
        }

        return worker.doWork(new GenericWorker.WorkerInterface() {
            @Override
            public boolean isCancelled() {
                return BaseAsyncTask.this.isCancelled();
            }

            @Override
            public void onProgress(int progress, int max) {
                BaseAsyncTask.this.publishProgress(progress,max);
            }
        }).getResults();
    }
}
