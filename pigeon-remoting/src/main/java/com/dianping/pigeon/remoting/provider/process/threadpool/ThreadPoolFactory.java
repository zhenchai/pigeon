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

/**
 * Created by chenchongze on 16/11/23.
 */
public class ThreadPoolFactory {

    private static final Logger logger = LoggerLoader.getLogger(ThreadPoolFactory.class);
    private static volatile boolean isInitialized = false;
    private static final Interner<PoolConfig> interner = Interners.newWeakInterner();

    static {
        init();
    }

    public static void init() {
        if (!isInitialized) {
            synchronized (ThreadPoolFactory.class) {
                if (!isInitialized) {
                    isInitialized = true;
                }
            }
        }
    }

    // poolConfig --> threadPool
    private static final ConcurrentMap<PoolConfig, ThreadPool> threadPools = Maps.newConcurrentMap();

    public static ThreadPool getThreadPool(PoolConfig poolConfig) {
        ThreadPool threadPool = threadPools.get(poolConfig);
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
        }

        return threadPool;
    }

}
