/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.threadpool.ThreadPool;

public interface ResponseProcessor {

	void stop();

	void processResponse(final InvocationResponse response, final Client client);

	String getProcessorStatistics();

	ThreadPool getResponseProcessThreadPool();
}
