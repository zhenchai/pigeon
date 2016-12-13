package com.dianping.pigeon.remoting.provider.process.threadpool;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.provider.config.PoolConfig;
import com.dianping.pigeon.threadpool.DynamicThreadPool;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 16/11/23.
 */
public class DynamicThreadPoolFactory {

    private static final Logger logger = LoggerLoader.getLogger(DynamicThreadPoolFactory.class);
    private static final Interner<PoolConfig> interner = Interners.newWeakInterner();
    // poolConfig --> threadPool
    private static final ConcurrentMap<PoolConfig, DynamicThreadPool> dynamicThreadPools = Maps.newConcurrentMap();

    // for lion poolConfig
    public static DynamicThreadPool getThreadPool(PoolConfig poolConfig) {
        DynamicThreadPool threadPool = dynamicThreadPools.get(poolConfig);

        if (threadPool != null && threadPool.getExecutor().isShutdown()) {
            return null;
        }

        return threadPool;
    }

    public static void refreshThreadPool(PoolConfig poolConfig) {
        DynamicThreadPool threadPool = dynamicThreadPools.get(poolConfig);
        if (threadPool == null) {
            synchronized (interner.intern(poolConfig)) {
                threadPool = dynamicThreadPools.get(poolConfig);
                if (threadPool == null) {
                    try {
                        threadPool = new DynamicThreadPool("Pigeon-Server-Request-Processor-" + poolConfig.getPoolName(),
                                poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(),
                                poolConfig.getWorkQueueSize());
                        dynamicThreadPools.put(poolConfig, threadPool);
                    } catch (Throwable t) {
                        logger.warn("Error while creating threadPool of " + poolConfig + ".", t);
                    }
                }
            }
        } else {
            if (poolConfigChanged(poolConfig, threadPool)) {
                synchronized (interner.intern(poolConfig)) {
                    threadPool = dynamicThreadPools.get(poolConfig);
                    if (threadPool != null && poolConfigChanged(poolConfig, threadPool)) {
                        threadPool.setCorePoolSize(poolConfig.getCorePoolSize());
                        threadPool.setMaximumPoolSize(poolConfig.getMaxPoolSize());
                        threadPool.setWorkQueueCapacity(poolConfig.getWorkQueueSize());
                    }
                }
            }
        }
    }

    public static void closeThreadPool(PoolConfig poolConfig) {
        DynamicThreadPool threadPool = dynamicThreadPools.get(poolConfig);
        if (threadPool != null) {
            synchronized (interner.intern(poolConfig)) {
                threadPool = dynamicThreadPools.get(poolConfig);
                if (threadPool != null) {
                    try {
                        threadPool.getExecutor().shutdown();
                        //threadPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                        dynamicThreadPools.remove(poolConfig);
                    } catch (Throwable t) {
                        logger.warn("Error when shutting down old pool.", t);
                    }
                }
            }
        }
    }

    private static boolean poolConfigChanged(PoolConfig poolConfig, DynamicThreadPool threadPool) {
        return poolConfig.getCorePoolSize() != threadPool.getCorePoolSize()
                || poolConfig.getMaxPoolSize() != threadPool.getMaximumPoolSize()
                || poolConfig.getWorkQueueSize() != threadPool.getWorkQueueCapacity();
    }

}
