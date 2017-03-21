/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public interface ThreadPool {

    void execute(Runnable run);

    <T> Future<T> submit(Callable<T> call);

    Future<?> submit(Runnable run);

    ThreadPoolExecutor getExecutor();

    void prestartAllCoreThreads();

    void allowCoreThreadTimeOut(boolean value);

}
