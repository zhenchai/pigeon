/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.threadpool.ThreadPool;

/**
 * 处理调用者的请求request
 * RequestProcessor是一个SPI扩展点，用于创建请求处理器
 */
public interface RequestProcessor {

    void start(ServerConfig serverConfig);

    void stop();

    Future<InvocationResponse> processRequest(final InvocationRequest request, final ProviderContext providerContext);

    String getProcessorStatistics();

    String getProcessorStatistics(final InvocationRequest request);

    String getProcessorStatistics(final ThreadPool threadPool);

    <T> void addService(ProviderConfig<T> providerConfig);

    <T> void removeService(ProviderConfig<T> providerConfig);

    boolean needCancelRequest(InvocationRequest request);

    ThreadPool getRequestProcessThreadPool();
}
