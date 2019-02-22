package com.dianping.pigeon.threadpool;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;

import java.util.concurrent.*;

/**
 * Created by chenchongze on 16/12/10.
 */

/**
 * 具体线程池
 */
public class DynamicThreadPool implements ThreadPool, ExecutorAware {

    private static final Logger logger = LoggerLoader.getLogger(DynamicThreadPool.class);

    private final String name;
    /**
     * executor
     */
    private final ThreadPoolExecutor executor;

    private final DefaultThreadFactory factory;
    private final ResizableBlockingQueue<Runnable> workQueue;

    /**
     * 初始化-动态线程池
     * @param poolName
     * @param corePoolSize
     * @param maximumPoolSize
     * @param workQueueCapacity
     */
    public DynamicThreadPool(String poolName, int corePoolSize, int maximumPoolSize, int workQueueCapacity) {
        this(poolName, corePoolSize, maximumPoolSize, workQueueCapacity,
                new ThreadPoolExecutor.AbortPolicy(), true, false);
    }

    /**
     * DynamicThreadPool是Pigeon中对ThreadPoolExecutor的封装，
     * 在Pigeon中主要用作Provider端请求的异步处理
     * @param poolName
     * @param corePoolSize
     * @param maximumPoolSize
     * @param workQueueCapacity
     * @param handler
     * @param prestartAllCoreThreads
     * @param allowCoreThreadTimeOut
     */
    public DynamicThreadPool(String poolName, int corePoolSize, int maximumPoolSize,
                             int workQueueCapacity, RejectedExecutionHandler handler,
                             boolean prestartAllCoreThreads, boolean allowCoreThreadTimeOut) {
        if (maximumPoolSize > 1000) {
            logger.warn("the 'maximumPoolSize' property is too big");
            maximumPoolSize = 1000;
        }
        if (corePoolSize > 300) {
            logger.warn("the 'corePoolSize' property is too big");
            corePoolSize = 300;
        }
        this.name = poolName;
        this.factory = new DefaultThreadFactory(this.name);
        this.workQueue = new ResizableLinkedBlockingQueue<Runnable>(workQueueCapacity);
        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 30, TimeUnit.SECONDS,
                workQueue, this.factory, handler);
        if (prestartAllCoreThreads) {
            this.executor.prestartAllCoreThreads();
        }
        this.executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
    }

    public void execute(Runnable run) {
        this.executor.execute(run);
    }

    public <T> Future<T> submit(Callable<T> call) {
        return this.executor.submit(call);
    }

    public Future<?> submit(Runnable run) {
        return this.executor.submit(run);
    }

    public ThreadPoolExecutor getExecutor() {
        return this.executor;
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        executor.setCorePoolSize(corePoolSize);
    }

    @Override
    public int getCorePoolSize() {
        return executor.getCorePoolSize();
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        executor.setMaximumPoolSize(maximumPoolSize);
    }

    @Override
    public int getMaximumPoolSize() {
        return executor.getMaximumPoolSize();
    }

    @Override
    public void setWorkQueueCapacity(int workQueueCapacity) {
        workQueue.setCapacity(workQueueCapacity);
    }

    @Override
    public int getWorkQueueCapacity() {
        return workQueue.getCapacity();
    }

    @Override
    public void prestartAllCoreThreads() {
        this.executor.prestartAllCoreThreads();
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        this.executor.allowCoreThreadTimeOut(value);
    }
}
