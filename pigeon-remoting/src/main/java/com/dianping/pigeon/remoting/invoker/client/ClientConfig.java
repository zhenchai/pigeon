package com.dianping.pigeon.remoting.invoker.client;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.remoting.common.util.Constants;

/**
 * @author qi.yin
 *         2016/11/02  下午1:49.
 */
public class ClientConfig {

    private ConfigManager configManager;

    private int connectTimeout;

    private int highWaterMark;

    private int lowWaterMark;

    private int bossThreadPoolCount;

    private int workerThreadPoolCount;

    private int initialSize;

    private int normalSize;

    private int maxActive;

    private int maxWait;

    private int timeBetweenCheckerMillis;

    private boolean heartbeated;

    private volatile int heartbeatTimeout;

    private volatile int deadThreshold;

    private volatile int healthThreshold;

    private int heartbeatInterval;

    private volatile boolean isHeartbeatAutoPickOff;

    public ClientConfig(ConfigManager configManager) {
        connectTimeout = configManager.getIntValue(Constants.KEY_NETTY_CONNECTTIMEOUT,
                Constants.DEFAULT_NETTY_CONNECTTIMEOUT);

        highWaterMark = configManager.getIntValue(Constants.KEY_CHANNEL_WRITEBUFFHIGH,
                Constants.DEFAULT_CHANNEL_WRITEBUFFHIGH);

        lowWaterMark = configManager.getIntValue(Constants.KEY_CHANNEL_WRITEBUFFLOW,
                Constants.DEFAULT_CHANNEL_WRITEBUFFLOW);

        bossThreadPoolCount = configManager.getIntValue(Constants.KEY_INVOKER_NETTYBOSSCOUNT,
                Constants.DEFAULT_INVOKER_NETTYBOSSCOUNT);

        workerThreadPoolCount = configManager.getIntValue(Constants.KEY_INVOKER_NETTYWORKERCOUNT,
                Constants.DEFAULT_INVOKER_NETTYWORKERCOUNT);

        initialSize = configManager.getIntValue(Constants.KEY_CHANNEL_POOL_INITIAL_SIZE,
                Constants.DEFAULT_CHANNEL_POOL_INITIAL_SIZE);

        normalSize = configManager.getIntValue(Constants.KEY_CHANNEL_POOL_NORMAL_SIZE,
                Constants.DEFAULT_CHANNEL_POOL_NORMAL_SIZE);

        maxActive = configManager.getIntValue(Constants.KEY_CHANNEL_POOL_MAX_ACTIVE,
                Constants.DEFAULT_CHANNEL_POOL_MAX_ACTIVE);

        maxWait = configManager.getIntValue(Constants.KEY_CHANNEL_POOL_MAX_WAIT,
                Constants.DEFAULT_CHANNEL_POOL_MAX_WAIT);

        timeBetweenCheckerMillis = configManager.getIntValue(Constants.KEY_CHANNEL_POOL_TIME_BETWEEN_CHECKER_MILLIS,
                Constants.DEFAULT_CHANNEL_POOL_TIME_BETWEEN_CHECKER_MILLIS);

        heartbeated = configManager.getBooleanValue(Constants.KEY_INVOKER_HEARTBEAT_ENABLE,
                Constants.DEFAULT_INVOKER_HEARTBEAT_ENABLE);

        heartbeatTimeout = configManager.getIntValue(Constants.KEY_HEARTBEAT_TIMEOUT,
                Constants.DEFAULT_HEARTBEAT_TIMEOUT);

        deadThreshold = configManager.getIntValue(Constants.KEY_HEARTBEAT_DEADTHRESHOLD,
                Constants.DEFAULT_HEARTBEAT_DEADTHRESHOLD);

        healthThreshold = configManager.getIntValue(Constants.KEY_HEARTBEAT_HEALTHTHRESHOLD,
                Constants.DEFAULT_HEARTBEAT_HEALTHTHRESHOLD);

        heartbeatInterval = configManager.getIntValue(Constants.KEY_HEARTBEAT_INTERVAL,
                Constants.DEFAULT_HEARTBEAT_INTERVAL);

        isHeartbeatAutoPickOff = configManager.getBooleanValue(Constants.KEY_HEARTBEAT_AUTOPICKOFF,
                Constants.DEFAULT_HEARTBEAT_AUTOPICKOFF);

        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getHighWaterMark() {
        return highWaterMark;
    }

    public void setHighWaterMark(int highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    public int getLowWaterMark() {
        return lowWaterMark;
    }

    public void setLowWaterMark(int lowWaterMark) {
        this.lowWaterMark = lowWaterMark;
    }

    public int getBossThreadPoolCount() {
        return bossThreadPoolCount;
    }

    public void setBossThreadPoolCount(int bossThreadPoolCount) {
        this.bossThreadPoolCount = bossThreadPoolCount;
    }

    public int getWorkerThreadPoolCount() {
        return workerThreadPoolCount;
    }

    public void setWorkerThreadPoolCount(int workerThreadPoolCount) {
        this.workerThreadPoolCount = workerThreadPoolCount;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getNormalSize() {
        return normalSize;
    }

    public void setNormalSize(int normalSize) {
        this.normalSize = normalSize;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeBetweenCheckerMillis() {
        return timeBetweenCheckerMillis;
    }

    public void setTimeBetweenCheckerMillis(int timeBetweenCheckerMillis) {
        this.timeBetweenCheckerMillis = timeBetweenCheckerMillis;
    }

    public boolean isHeartbeated() {
        return heartbeated;
    }

    public void setHeartbeated(boolean heartbeated) {
        this.heartbeated = heartbeated;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public int getDeadThreshold() {
        return deadThreshold;
    }

    public void setDeadThreshold(int deadThreshold) {
        this.deadThreshold = deadThreshold;
    }

    public int getHealthThreshold() {
        return healthThreshold;
    }

    public void setHealthThreshold(int healthThreshold) {
        this.healthThreshold = healthThreshold;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public boolean isHeartbeatAutoPickOff() {
        return isHeartbeatAutoPickOff;
    }

    public void setIsHeartbeatAutoPickOff(boolean isHeartbeatAutoPickOff) {
        this.isHeartbeatAutoPickOff = isHeartbeatAutoPickOff;
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith(Constants.KEY_HEARTBEAT_TIMEOUT)) {
                try {
                    heartbeatTimeout = Integer.valueOf(value);
                } catch (RuntimeException e) {
                }
            } else if (key.endsWith(Constants.KEY_HEARTBEAT_HEALTHTHRESHOLD)) {
                try {
                    healthThreshold = Integer.valueOf(value);
                } catch (RuntimeException e) {
                }
            } else if (key.endsWith(Constants.KEY_HEARTBEAT_DEADTHRESHOLD)) {
                try {
                    deadThreshold = Integer.valueOf(value);
                } catch (RuntimeException e) {
                }
            } else if (key.endsWith(Constants.KEY_HEARTBEAT_AUTOPICKOFF)) {
                try {
                    isHeartbeatAutoPickOff = Boolean.valueOf(value);
                } catch (RuntimeException e) {
                }
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onKeyRemoved(String key) {
            // TODO Auto-generated method stub

        }

    }
}
