package com.dianping.pigeon.remoting.provider.process.threadpool;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.provider.config.PoolConfig;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 16/11/23.
 */
public class ThreadPoolFactory {

    private static final Logger logger = LoggerLoader.getLogger(ThreadPoolFactory.class);
    private static final Interner<PoolConfig> interner = Interners.newWeakInterner();
    private static volatile boolean isInitialized = false;

    static {
        init();
    }

    public static void init() {
        if (!isInitialized) {
            synchronized (ThreadPoolFactory.class) {
                if (!isInitialized) {
                    //todo ??

                    isInitialized = true;
                }
            }
        }
    }

    // poolConfig --> threadPool
    private static final ConcurrentMap<PoolConfig, ThreadPool> threadPools = Maps.newConcurrentMap();

    public static ThreadPool getThreadPool(PoolConfig poolConfig) {
        ThreadPool threadPool = threadPools.get(poolConfig);
        try {
            if (threadPool == null) {
                synchronized (interner.intern(poolConfig)) {
                    threadPool = threadPools.get(poolConfig);
                    if (threadPool == null) {
                        threadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-" + poolConfig,
                                poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(),
                                new LinkedBlockingQueue<Runnable>(poolConfig.getWorkQueueSize()));
                        threadPools.put(poolConfig, threadPool);
                    }
                }
            } else {
                if (poolConfig.getWorkQueueSize() != (threadPool.getExecutor().getQueue().size()
                        + threadPool.getExecutor().getQueue().remainingCapacity())) {
                    synchronized (interner.intern(poolConfig)) {
                        threadPool = threadPools.get(poolConfig);
                        if (threadPool != null
                                && poolConfig.getWorkQueueSize()
                                != (threadPool.getExecutor().getQueue().size()
                                + threadPool.getExecutor().getQueue().remainingCapacity())) {
                            threadPool.getExecutor().shutdown();
                            threadPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                            threadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-" + poolConfig,
                                    poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(),
                                    new LinkedBlockingQueue<Runnable>(poolConfig.getWorkQueueSize()));
                        }
                    }
                }else if (poolConfig.getCorePoolSize() != threadPool.getExecutor().getCorePoolSize()
                        || poolConfig.getMaxPoolSize() != threadPool.getExecutor().getMaximumPoolSize()) {
                    synchronized (interner.intern(poolConfig)) {
                        threadPool = threadPools.get(poolConfig);
                        if (threadPool != null
                                && (poolConfig.getCorePoolSize() != threadPool.getExecutor().getCorePoolSize()
                                || poolConfig.getMaxPoolSize() != threadPool.getExecutor().getMaximumPoolSize())) {
                            threadPool.getExecutor().setCorePoolSize(poolConfig.getCorePoolSize());
                            threadPool.getExecutor().setMaximumPoolSize(poolConfig.getMaxPoolSize());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.warn("Error while getting threadPool of " + poolConfig + ".", t);
        }

        return threadPool;
    }

    public static void closeThreadPool(PoolConfig poolConfig) {
        ThreadPool threadPool = threadPools.get(poolConfig);
        if (threadPool != null) {
            synchronized (interner.intern(poolConfig)) {
                threadPool = threadPools.get(poolConfig);
                if (threadPool != null) {
                    try {
                        threadPool.getExecutor().shutdown();
                        threadPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                        threadPools.remove(poolConfig);
                    } catch (Throwable t) {
                        logger.warn("Error when shutting down old pool.", t);
                    }
                }
            }
        }
    }

}
