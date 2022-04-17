/*
 *     Copyright 2020 - 2022 Tyler Williamson
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

package com.ominous.tylerutils.work;

import java.util.concurrent.CountDownLatch;

public class ParallelThreadManager {
    public static void execute(Runnable... runnables) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(runnables.length);

        for (Runnable runnable : runnables) {
            new ParallelThread(countDownLatch, runnable).start();
        }

        countDownLatch.await();
    }

    private static class ParallelThread extends Thread {
        private final CountDownLatch countDownLatch;

        ParallelThread(CountDownLatch countDownLatch, Runnable runnable) {
            super(runnable);
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            super.run();

            countDownLatch.countDown();
        }
    }
}
