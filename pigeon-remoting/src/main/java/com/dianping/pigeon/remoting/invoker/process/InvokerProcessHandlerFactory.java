/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.process.filter.*;

public final class InvokerProcessHandlerFactory {

	//责任链模式
	private static List<InvocationInvokeFilter> bizProcessFilters = new LinkedList<InvocationInvokeFilter>();

	private static ServiceInvocationHandler bizInvocationHandler = null;

	private static volatile boolean isInitialized = false;

	public static void init() {
		if (!isInitialized) {
			if (Constants.MONITOR_ENABLE) {
				registerBizProcessFilter(new RemoteCallMonitorInvokeFilter());
			}
			registerBizProcessFilter(new TraceFilter());
			registerBizProcessFilter(new FaultInjectionFilter());
			registerBizProcessFilter(new DegradationFilter());
			//集群策略
			registerBizProcessFilter(new ClusterInvokeFilter());
			//网关，实际上只负责 统计下
			registerBizProcessFilter(new GatewayInvokeFilter());
			//将调用环境（invokerContext）设置到请求（invocationRequest）
			registerBizProcessFilter(new ContextPrepareInvokeFilter());
			registerBizProcessFilter(new SecurityFilter());
			//发出请求
			registerBizProcessFilter(new RemoteCallInvokeFilter());
			bizInvocationHandler = createInvocationHandler(bizProcessFilters);
			isInitialized = true;
		}
	}

	public static ServiceInvocationHandler selectInvocationHandler(InvokerConfig<?> invokerConfig) {
		return bizInvocationHandler;
	}

	@SuppressWarnings({ "rawtypes" })
	private static <V extends ServiceInvocationFilter> ServiceInvocationHandler createInvocationHandler(
			List<V> internalFilters) {
		ServiceInvocationHandler last = null;
		List<V> filterList = new ArrayList<V>();
		filterList.addAll(internalFilters);
		for (int i = filterList.size() - 1; i >= 0; i--) {
			final V filter = filterList.get(i);
			final ServiceInvocationHandler next = last;
			last = new ServiceInvocationHandler() {
				@SuppressWarnings("unchecked")
				@Override
				public InvocationResponse handle(InvocationContext invocationContext) throws Throwable {
					InvocationResponse resp = filter.invoke(next, invocationContext);
					return resp;
				}
			};
		}
		return last;
	}

	public static void registerBizProcessFilter(InvocationInvokeFilter filter) {
		bizProcessFilters.add(filter);
	}

	public static void clearClientFilters() {
		bizProcessFilters.clear();
	}

}
