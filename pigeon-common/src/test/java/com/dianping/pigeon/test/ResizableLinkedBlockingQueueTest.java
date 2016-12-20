package com.dianping.pigeon.test;

import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ResizableLinkedBlockingQueue;
import com.dianping.pigeon.threadpool.ThreadPool;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by chenchongze on 16/12/10.
 */
public class ResizableLinkedBlockingQueueTest {

    @Test
    public void test() {
        final int round = 20;
        final long offerSpeed = 700;
        final long pollSpeed = 1400;
        final long refreshSpeed = 350;
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
                        Thread.sleep(refreshSpeed);
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

    @Test
    public void test1() {
        final int round = 20;
        final long offerSpeed = 700;
        final long pollSpeed = 1400;
        final long refreshSpeed = 350;
        final int initQueueCapacity = 100;
        ThreadPool jobPool = new DefaultThreadPool("job");
        ThreadPool offerPool = new DefaultThreadPool("offerPool", round, round);
        ThreadPool pollPool = new DefaultThreadPool("pollPool", round, round);

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>(initQueueCapacity);

        jobPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println("size: " + queue.size() + ", capacity: " + queue.remainingCapacity());
                        Thread.sleep(refreshSpeed);
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

    @Test
    public void test2() {
        final int round = 3000;
        final long offerSpeed = 1;
        final long pollSpeed = 1;
        final long refreshSpeed = 1;
        final int initQueueCapacity = 100;
        ThreadPool jobPool = new DefaultThreadPool("job");
        ThreadPool offerPool = new DefaultThreadPool("offerPool", round, round);
        ThreadPool pollPool = new DefaultThreadPool("pollPool", round, round);

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>(initQueueCapacity);

        jobPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (initQueueCapacity != (queue.size() + queue.remainingCapacity())) {
                            System.out.println("oh no");
                        }
                        Thread.sleep(1);
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
