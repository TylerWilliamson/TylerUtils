/*
 *     Copyright 2020 - 2021 Tyler Williamson
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

package com.ominous.tylerutils.async;

import java.util.LinkedList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;

public class Promise<S,T> {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Promise<?,S> parent;
    private final PromiseCallable<S,T> runCallable;
    private final VoidPromiseCallable<Throwable> catchCallable;
    private final LinkedList<Promise<T,?>> nextList = new LinkedList<>();
    private final CountDownLatch startCountDown = new CountDownLatch(1);
    private T result;
    private Future<T> resultFuture;

    private PromiseState state = PromiseState.NOT_STARTED;

    private Promise(Promise<?,S> parent,@NonNull PromiseCallable<S,T> runCallable, VoidPromiseCallable<Throwable> catchCallable) {
        this.parent = parent;
        this.runCallable = runCallable;
        this.catchCallable = catchCallable;

        start();
    }

    public static <U> Promise<Void, U> create(final U input) {
        return create((a) -> input, null);
    }

    public static <U> Promise<Void, U> create(@NonNull PromiseCallable<Void,U> runCallable) {
        return create(runCallable, null);
    }

    public static Promise<Void, Void> create(@NonNull VoidPromiseCallable<Void> runCallable) {
        return create(convertToCallable(runCallable), null);
    }

    public static <U> Promise<Void, U> create(@NonNull PromiseCallable<Void,U> runCallable, VoidPromiseCallable<Throwable> catchCallable) {
        return new Promise<>(null,runCallable,catchCallable);
    }

    public static Promise<Void, Void> create(@NonNull VoidPromiseCallable<Void> runCallable, VoidPromiseCallable<Throwable> catchCallable) {
        return new Promise<>(null,convertToCallable(runCallable),catchCallable);
    }

    public <U> Promise<T,U> then(@NonNull PromiseCallable<T,U> runCallable) {
        return then(runCallable,null);
    }

    public Promise<T,Void> then(@NonNull VoidPromiseCallable<T> runCallable) {
        return then(convertToCallable(runCallable),null);
    }

    public <U> Promise<T,U> then(@NonNull PromiseCallable<T,U> runCallable, VoidPromiseCallable<Throwable> catchCallable) {
        Promise<T,U> then = new Promise<>(this, runCallable, catchCallable);

        nextList.add(then);

        then.start();

        return then;
    }

    public Promise<T,Void> then(@NonNull VoidPromiseCallable<T> runCallable, VoidPromiseCallable<Throwable> catchCallable) {
        Promise<T,Void> then = new Promise<>(this, convertToCallable(runCallable), catchCallable);

        nextList.add(then);

        then.start();

        return then;
    }

    private void start() {
        if (parent == null) {
            //Then this IS first, and the parent takes a void parameter
            executor.submit(() -> run(null));
        } else if (parent.getState() == PromiseState.COMPLETED) {
            executor.submit(() -> run(parent.getResult()));
        } else if (parent.getState() == PromiseState.FAILED ||
                parent.getState() == PromiseState.CANCELLED) {
            state = PromiseState.CANCELLED;
        }
    }

    private void run(S input) {
        if (state == PromiseState.NOT_STARTED) {
            state = PromiseState.STARTED;

            resultFuture = executor.submit(() -> runCallable.call(input));
            startCountDown.countDown();

            try {
                result = resultFuture.get();

                state = PromiseState.COMPLETED;
            } catch (ExecutionException e) {
                if (catchCallable != null) {
                    executor.submit(() -> {
                        try {
                            catchCallable.call(e.getCause());
                        } catch (Exception ignored) {
                            //ignored
                        }
                    });
                }

                state = PromiseState.FAILED;
            } catch (InterruptedException | CancellationException e) {
                state = PromiseState.CANCELLED;
            }

            for (Promise<T,?> next : nextList) {
                if (state == PromiseState.COMPLETED) {
                    next.run(result);
                } else {
                    next.cancel(true);
                }
            }
        }
    }

    //TODO cancel siblings
    public void cancel(boolean mayInterruptIfRunning) {
        if (state == PromiseState.NOT_STARTED || state == PromiseState.STARTED) {
            state = PromiseState.CANCELLED;

            if (resultFuture != null && mayInterruptIfRunning) {
                resultFuture.cancel(true);
            }

            if (parent != null) {
                parent.cancel(mayInterruptIfRunning);
            }

            for (Promise<T,?> next : nextList) {
                next.cancel(mayInterruptIfRunning);
            }
        }
    }

    public T getResult() {
        return result;
    }

    public PromiseState getState() {
        return state;
    }

    public T await() throws ExecutionException, InterruptedException {
        startCountDown.await();

        return resultFuture.get();
    }

    public enum PromiseState {
        NOT_STARTED,
        STARTED,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    private static <U> PromiseCallable<U, Void> convertToCallable(VoidPromiseCallable<U> voidPromiseCallable) {
        return (a) -> {
            voidPromiseCallable.call(a);

            return null;
        };
    }

    public interface PromiseCallable<S,T> {
        T call(S input) throws Exception;
    }

    public interface VoidPromiseCallable<S> {
        void call(S input) throws Exception;
    }
}