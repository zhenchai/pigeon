package com.dianping.pigeon.remoting.provider.config.spring;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 16/10/15.
 */
public class PoolBean {

    private static Logger logger = LoggerLoader.getLogger(PoolBean.class);

    private ThreadPool threadPool = null;

    private String poolName;
    private int corePoolSize;
    private int maxPoolSize;
    private int workQueueSize;


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

    @Override
    public String toString() {
        return "PoolBean{" +
                "workQueueSize=" + workQueueSize +
                ", maxPoolSize=" + maxPoolSize +
                ", corePoolSize=" + corePoolSize +
                ", poolName='" + poolName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoolBean poolBean = (PoolBean) o;

        if (corePoolSize != poolBean.corePoolSize) return false;
        if (maxPoolSize != poolBean.maxPoolSize) return false;
        if (workQueueSize != poolBean.workQueueSize) return false;
        return poolName != null ? poolName.equals(poolBean.poolName) : poolBean.poolName == null;

    }

    @Override
    public int hashCode() {
        int result = poolName != null ? poolName.hashCode() : 0;
        result = 31 * result + corePoolSize;
        result = 31 * result + maxPoolSize;
        result = 31 * result + workQueueSize;
        return result;
    }

    public boolean checkNotNull() {
        if (StringUtils.isNotBlank(poolName) && corePoolSize >= 0 && maxPoolSize > 0 && workQueueSize > 0) {
            return true;
        }
        return false;
    }

    public void closeThreadPool() {
        if (threadPool != null) {
            synchronized (this) {
                if (threadPool != null) {
                    try {
                        threadPool.getExecutor().shutdown();
                        threadPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                        threadPool = null;
                    } catch (Throwable t) {
                        logger.warn("error when shutting down old pool", t);
                    }
                }
            }
        }
    }

    public ThreadPool getRefreshedThreadPool() {
        if (threadPool == null) {
            synchronized (this) {
                if (threadPool == null) {
                    threadPool = new DefaultThreadPool(
                            "Pigeon-Server-Request-Processor-" + poolName, corePoolSize, maxPoolSize,
                            new LinkedBlockingQueue<Runnable>(workQueueSize));
                }
            }
        } else if ((corePoolSize != threadPool.getExecutor().getCorePoolSize())
                || (maxPoolSize != threadPool.getExecutor().getMaximumPoolSize())) {
            synchronized (this) {
                if(threadPool != null && (corePoolSize != threadPool.getExecutor().getCorePoolSize())
                        || (maxPoolSize != threadPool.getExecutor().getMaximumPoolSize())
                        && corePoolSize >= 0 && maxPoolSize > 0 && maxPoolSize >= corePoolSize) {
                    threadPool.getExecutor().setCorePoolSize(corePoolSize);
                    threadPool.getExecutor().setMaximumPoolSize(maxPoolSize);
                }
            }
        } else if (threadPool != null && workQueueSize
                != (threadPool.getExecutor().getQueue().size()
                + threadPool.getExecutor().getQueue().remainingCapacity())) {
            synchronized (this) {
                if (threadPool != null && workQueueSize > 0 && workQueueSize
                        != (threadPool.getExecutor().getQueue().size()
                        + threadPool.getExecutor().getQueue().remainingCapacity())) {
                    try {
                        threadPool.getExecutor().shutdown();
                        threadPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                        threadPool = new DefaultThreadPool(
                                "Pigeon-Server-Request-Processor-" + poolName, corePoolSize, maxPoolSize,
                                new LinkedBlockingQueue<Runnable>(workQueueSize));
                    } catch (Throwable t) {
                        logger.warn("error when shutting down old pool", t);
                    }
                }
            }
        }
        return threadPool;
    }
}
