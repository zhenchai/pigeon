package com.dianping.pigeon.remoting.provider.config;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.provider.process.threadpool.DynamicThreadPoolFactory;
import com.dianping.pigeon.threadpool.DynamicThreadPool;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 16/11/28.
 */
public class PoolConfigFactory {

    private static final Logger logger = LoggerLoader.getLogger(PoolConfigFactory.class);
    // poolName --> poolConfig
    private static final ConcurrentMap<String, PoolConfig> poolConfigs = Maps.newConcurrentMap();
    // poolName --> (core/max/queue)key
    private static final ConcurrentMap<String, String> coreSizeKeys = Maps.newConcurrentMap();
    private static final ConcurrentMap<String, String> maxSizeKeys = Maps.newConcurrentMap();
    private static final ConcurrentMap<String, String> queueSizeKeys = Maps.newConcurrentMap();

    static {
        ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
    }

    public static PoolConfig createPoolConfig(String poolName,
                                              int coreSize,
                                              int maxSize,
                                              int queueSize) {
        PoolConfig poolConfig = poolConfigs.get(poolName);

        if (poolConfig == null) {
            poolConfig = new PoolConfig();
            poolConfig.setPoolName(poolName);
            poolConfig.setCorePoolSize(coreSize);
            poolConfig.setMaxPoolSize(maxSize);
            poolConfig.setWorkQueueSize(queueSize);
            poolConfigs.put(poolName, poolConfig);
        }

        return poolConfig;
    }

    public static boolean validate(PoolConfig poolConfig) {
        if (StringUtils.isNotBlank(poolConfig.getPoolName())
                && poolConfig.getCorePoolSize() >= 0
                && poolConfig.getMaxPoolSize() > 0
                && poolConfig.getMaxPoolSize() >= poolConfig.getCorePoolSize()
                && poolConfig.getWorkQueueSize() > 0) {
            return true;
        }
        return false;
    }

    public static ConcurrentMap<String, String> getCoreSizeKeys() {
        return coreSizeKeys;
    }

    public static ConcurrentMap<String, String> getMaxSizeKeys() {
        return maxSizeKeys;
    }

    public static ConcurrentMap<String, String> getQueueSizeKeys() {
        return queueSizeKeys;
    }

    private static class InnerConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onKeyUpdated(String key, String value) {
            for (Map.Entry<String, String> entry : coreSizeKeys.entrySet()) {
                String poolName = entry.getKey();
                String coreSizeKey = entry.getValue();
                if (key.endsWith(coreSizeKey)) {
                    try {
                        Integer coreSize = Integer.parseInt(value);
                        PoolConfig poolConfig = poolConfigs.get(poolName);
                        if (poolConfig != null) {
                            if (coreSize < 0 || coreSize > poolConfig.getMaxPoolSize()) {
                                throw new IllegalArgumentException("core size is illegal, please check: " + coreSize);
                            }
                            poolConfig.setCorePoolSize(coreSize);
                            DynamicThreadPoolFactory.refreshThreadPool(poolConfig);
                        }
                        logger.info("changed core size of pool: " + poolName + ", key: " + key + ", value: " + value);
                    } catch (RuntimeException e) {
                        logger.error("error while changing pool, key:" + key + ", value:" + value, e);
                    }
                }
            }

            for (Map.Entry<String, String> entry : maxSizeKeys.entrySet()) {
                String poolName = entry.getKey();
                String maxSizeKey = entry.getValue();
                if (key.endsWith(maxSizeKey)) {
                    try {
                        Integer maxSize = Integer.parseInt(value);
                        PoolConfig poolConfig = poolConfigs.get(poolName);
                        if (poolConfig != null) {
                            if (maxSize < poolConfig.getCorePoolSize() || maxSize <= 0) {
                                throw new IllegalArgumentException("max size is illegal, please check: " + maxSize);
                            }
                            poolConfig.setMaxPoolSize(maxSize);
                            DynamicThreadPoolFactory.refreshThreadPool(poolConfig);
                        }
                        logger.info("changed max size of pool: " + poolName + ", key: " + key + ", value: " + value);
                    } catch (RuntimeException e) {
                        logger.error("error while changing pool, key:" + key + ", value:" + value, e);
                    }
                }
            }

            for (Map.Entry<String, String> entry : queueSizeKeys.entrySet()) {
                String poolName = entry.getKey();
                String queueSizeKey = entry.getValue();
                if (key.endsWith(queueSizeKey)) {
                    try {
                        Integer queueSize = Integer.parseInt(value);
                        PoolConfig poolConfig = poolConfigs.get(poolName);
                        if (poolConfig != null) {
                            if (queueSize < 0) {
                                throw new IllegalArgumentException("queue size is illegal, please check: " + queueSize);
                            }
                            poolConfig.setWorkQueueSize(queueSize);
                            DynamicThreadPoolFactory.refreshThreadPool(poolConfig);
                        }
                        logger.info("changed queue size of pool: " + poolName + ", key: " + key + ", value: " + value);
                    } catch (RuntimeException e) {
                        logger.error("error while changing pool, key:" + key + ", value:" + value, e);
                    }
                }
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }
}
