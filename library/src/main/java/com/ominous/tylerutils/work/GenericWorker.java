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

import android.content.Context;

import androidx.annotation.Nullable;

@SuppressWarnings("rawtypes")
public abstract class GenericWorker<T extends GenericResults> {
    private final Context context;

    public GenericWorker(Context context) {
        this.context = context;
    }

    @SuppressWarnings("RedundantThrows")
    public abstract T doWork(WorkerInterface workerInterface) throws Throwable;

    public Context getContext() {
        return context;
    }

    public interface WorkerInterface {
        boolean isCancelled();

        void onProgress(int progress, int max);
    }

    public interface WorkerFactory<T extends GenericWorker> {
        @Nullable
        T getWorker(Context context);
    }
}
