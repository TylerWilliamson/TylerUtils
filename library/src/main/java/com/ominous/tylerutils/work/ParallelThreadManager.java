package com.ominous.tylerutils.work;

import java.util.concurrent.CountDownLatch;

public class ParallelThreadManager {
    private static class ParallelThread extends Thread {
        private CountDownLatch countDownLatch;

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

    public static void execute(Runnable... runnables) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(runnables.length);

        for(Runnable runnable : runnables) {
            new ParallelThread(countDownLatch,runnable).start();
        }

        countDownLatch.await();
    }
}
