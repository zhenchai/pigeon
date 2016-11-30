package com.dianping.pigeon.remoting.provider.config;

/**
 * Created by chenchongze on 16/11/23.
 */
public class PoolConfig {

    private String poolName;
    private volatile int corePoolSize;
    private volatile int maxPoolSize;
    private volatile int workQueueSize;

    public PoolConfig() {

    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getPoolName() {
        return poolName;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getWorkQueueSize() {
        return workQueueSize;
    }

    public void setWorkQueueSize(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }
}
