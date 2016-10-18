package com.dianping.pigeon.remoting.provider.config.spring;

import com.dianping.pigeon.remoting.common.util.Constants;

/**
 * Created by chenchongze on 16/10/15.
 */
public class PoolBean {

    private int corePoolSize = Constants.PROVIDER_POOL_CORE_SIZE;
    private int maxPoolSize = Constants.PROVIDER_POOL_MAX_SIZE;
    private int workQueueSize = Constants.PROVIDER_POOL_QUEUE_SIZE;

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
