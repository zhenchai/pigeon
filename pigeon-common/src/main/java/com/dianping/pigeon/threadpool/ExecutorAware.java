package com.dianping.pigeon.threadpool;

/**
 * Created by chenchongze on 16/12/10.
 */
public interface ExecutorAware {

    void setCorePoolSize(int corePoolSize);

    int getCorePoolSize();

    void setMaximumPoolSize(int maximumPoolSize);

    int getMaximumPoolSize();

    void setWorkQueueCapacity(int workQueueCapacity);

    int getWorkQueueCapacity();
}
