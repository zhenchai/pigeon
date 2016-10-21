package com.dianping.pigeon.remoting.provider.config.spring;

import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by chenchongze on 16/10/15.
 */
public class PoolBean {

    private ThreadPool threadPool = null;

    private String poolName;
    private int corePoolSize = Constants.PROVIDER_POOL_CORE_SIZE;
    private int maxPoolSize = Constants.PROVIDER_POOL_MAX_SIZE;
    private int workQueueSize = Constants.PROVIDER_POOL_QUEUE_SIZE;

    public ThreadPool getThreadPool() {
        if (threadPool == null) {
            synchronized (this) {
                if (threadPool == null) {
                    threadPool = new DefaultThreadPool(
                            "Pigeon-Server-Request-Processor-" + poolName, corePoolSize, maxPoolSize,
                            new LinkedBlockingQueue<Runnable>(workQueueSize));
                }
            }
        }
        return threadPool;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
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
