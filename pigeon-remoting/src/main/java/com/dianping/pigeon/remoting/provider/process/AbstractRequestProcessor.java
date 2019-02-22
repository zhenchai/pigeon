/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.listener.RequestTimeoutListener;
import com.dianping.pigeon.remoting.provider.process.threadpool.RequestThreadPoolProcessor;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.ThreadPoolUtils;

public abstract class AbstractRequestProcessor implements RequestProcessor {

	private static ThreadPool timeCheckThreadPool = new DefaultThreadPool("Pigeon-Provider-Timeout-Checker");

	/**
	 * 请求上下文
	 * Map<客户端传入的请求，请求上下文(包括获取请求的channel，时间线打点等等)>
	 */
	protected Map<InvocationRequest, ProviderContext> requestContextMap = new ConcurrentHashMap<InvocationRequest, ProviderContext>();

	protected static final Logger logger = LoggerLoader.getLogger(RequestThreadPoolProcessor.class);

	protected RequestTimeoutListener requestTimeoutListener = new RequestTimeoutListener(this, requestContextMap);

	protected volatile ServerConfig serverConfig;

	public AbstractRequestProcessor() {
		timeCheckThreadPool.execute(requestTimeoutListener);
	}

	public abstract Future<InvocationResponse> doProcessRequest(final InvocationRequest request,
			final ProviderContext providerContext);

	public abstract void doStart();

	public void start(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
		doStart();
	}

	public abstract void doStop();

	public void stop() {
		ThreadPoolUtils.shutdown(timeCheckThreadPool.getExecutor());
		doStop();
	}

	public Map<InvocationRequest, ProviderContext> getRequestContextMap() {
		return requestContextMap;
	}

	public Future<InvocationResponse> processRequest(final InvocationRequest request,
			final ProviderContext providerContext) {
		if (request.getCreateMillisTime() == 0) {
			request.setCreateMillisTime(System.currentTimeMillis());
		}
		Future<InvocationResponse> invocationResponse = null;
		try {
			invocationResponse = doProcessRequest(request, providerContext);
		} catch (Throwable e) {
			String msg = "process request failed:" + request;
			if (request.getCallType() == Constants.CALLTYPE_REPLY
					&& request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
				providerContext.getChannel().write(providerContext, ProviderUtils.createFailResponse(request, e));
			}
			// logger.error(msg, e);
		}
		providerContext.setFuture(invocationResponse);
		return invocationResponse;
	}

}
