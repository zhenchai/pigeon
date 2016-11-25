package com.dianping.pigeon.remoting.provider.config;

/**
 * Created by chenchongze on 16/11/23.
 */
public class PoolConfig {

    private final String poolName;
    private final PoolConfigSource source;
    private volatile int corePoolSize;
    private volatile int maxPoolSize;
    private volatile int workQueueSize;

    public PoolConfig(String poolName, PoolConfigSource source) {
        this.poolName = poolName;
        this.source = source;
    }

    public String getPoolName() {
        return poolName;
    }

    public PoolConfigSource getSource() {
        return source;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoolConfig that = (PoolConfig) o;

        if (!poolName.equals(that.poolName)) return false;
        return source == that.source;

    }

    @Override
    public int hashCode() {
        int result = poolName.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PoolConfig{" +
                "poolName='" + poolName + '\'' +
                ", source=" + source +
                '}';
    }
}
