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

    public static DynamicThreadPool getThreadPool(PoolConfig poolConfig) {
        DynamicThreadPool threadPool = dynamicThreadPools.get(poolConfig);
        try {
            if (threadPool == null) {
                synchronized (interner.intern(poolConfig)) {
                    threadPool = dynamicThreadPools.get(poolConfig);
                    if (threadPool == null) {
                        threadPool = new DynamicThreadPool("Pigeon-Server-Request-Processor-" + poolConfig,
                                poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(),
                                poolConfig.getWorkQueueSize());
                        dynamicThreadPools.put(poolConfig, threadPool);
                    }
                }
            } else {
                if (poolConfig.getWorkQueueSize() != threadPool.getWorkQueueCapacity()) {
                    synchronized (interner.intern(poolConfig)) {
                        threadPool = dynamicThreadPools.get(poolConfig);
                        if (threadPool != null
                                && poolConfig.getWorkQueueSize() != threadPool.getWorkQueueCapacity()) {
                            threadPool.setWorkQueueCapacity(poolConfig.getWorkQueueSize());
                        }
                    }
                }else if (poolConfig.getCorePoolSize() != threadPool.getExecutor().getCorePoolSize()
                        || poolConfig.getMaxPoolSize() != threadPool.getExecutor().getMaximumPoolSize()) {
                    synchronized (interner.intern(poolConfig)) {
                        threadPool = dynamicThreadPools.get(poolConfig);
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
        DynamicThreadPool threadPool = dynamicThreadPools.get(poolConfig);
        if (threadPool != null) {
            synchronized (interner.intern(poolConfig)) {
                threadPool = dynamicThreadPools.get(poolConfig);
                if (threadPool != null) {
                    try {
                        threadPool.getExecutor().shutdown();
                        dynamicThreadPools.remove(poolConfig);
                        threadPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                    } catch (Throwable t) {
                        logger.warn("Error when shutting down old pool.", t);
                    }
                }
            }
        }
    }

}
