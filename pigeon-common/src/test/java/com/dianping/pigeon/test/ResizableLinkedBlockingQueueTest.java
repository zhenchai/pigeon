package com.dianping.pigeon.test;

import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ResizableLinkedBlockingQueue;
import com.dianping.pigeon.threadpool.ThreadPool;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by chenchongze on 16/12/10.
 */
public class ResizableLinkedBlockingQueueTest {

    @Test
    public void test() {
        final int round = 20;
        final long offerSpeed = 700;
        final long pollSpeed = 1300;
        final long resizeSpeed = 10000;
        final int resizeStep = 25;
        final int initQueueCapacity = 100;
        ThreadPool jobPool = new DefaultThreadPool("job");
        ThreadPool offerPool = new DefaultThreadPool("offerPool", round, round);
        ThreadPool pollPool = new DefaultThreadPool("pollPool", round, round);

        final ResizableLinkedBlockingQueue<Object> queue = new ResizableLinkedBlockingQueue<>(initQueueCapacity);

        jobPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println("size: " + queue.size() + ", capacity: " + queue.getCapacity());
                        Thread.sleep(pollSpeed);
                    } catch (InterruptedException e) {
                        //
                    }
                }
            }
        });

        jobPool.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0, capacity = initQueueCapacity; ; ++i) {
                    try {
                        Thread.sleep(resizeSpeed);
                        capacity -= resizeStep;
                        if (capacity <= 0) {
                            capacity = initQueueCapacity;
                        }
                        queue.setCapacity(capacity);
                    } catch (InterruptedException e) {
                        //
                    }
                }
            }
        });

        for (int i = 0; i < 20; ++i) {

            offerPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            queue.offer(new Object());
                            Thread.sleep(offerSpeed);
                        } catch (InterruptedException e) {
                            //
                        }
                    }
                }
            });

            pollPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            queue.poll();
                            Thread.sleep(pollSpeed);
                        } catch (InterruptedException e) {
                            //
                        }
                    }
                }
            });
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
