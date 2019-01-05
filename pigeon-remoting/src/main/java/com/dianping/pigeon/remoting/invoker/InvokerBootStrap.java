/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.invoker.process.InvokerProcessHandlerFactory;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.util.VersionUtils;

public final class InvokerBootStrap {

	private static final Logger logger = LoggerLoader.getLogger(InvokerBootStrap.class);

	private static volatile boolean isStartup = false;

	public static boolean isStartup() {
		return isStartup;
	}

	/**
	 * 调用服务 启动
	 */
	public static void startup() {
		if (!isStartup) {
			synchronized (InvokerBootStrap.class) {
				if (!isStartup) {
					//初始化，保存了所有的未返回的调用信息
					ServiceInvocationRepository.getInstance().init();
					//调用process的处理工厂
					InvokerProcessHandlerFactory.init();
					SerializerFactory.init();
					LoadBalanceManager.init();
					RegionPolicyManager.INSTANCE.init();
					Monitor monitor = MonitorLoader.getMonitor();
					if (monitor != null) {
						monitor.init();
					}

					ResponseProcessorFactory.selectProcessor().getResponseProcessThreadPool().prestartAllCoreThreads();
					isStartup = true;
					logger.warn("pigeon client[version:" + VersionUtils.VERSION + "] has been started");
				}
			}
		}
	}

	public static void shutdown() {
		if (isStartup) {
			synchronized (InvokerBootStrap.class) {
				if (isStartup) {
					try {
						ClientManager.getInstance().destroy();
					} catch (Throwable e) {
					}
					try {
						ServiceInvocationRepository.getInstance().destroy();
					} catch (Throwable e) {
					}
					try {
						ResponseProcessorFactory.stop();
					} catch (Throwable e) {
					}
					try {
						LoadBalanceManager.destroy();
					} catch (Throwable e) {
					}
					isStartup = false;
					if (logger.isInfoEnabled()) {
						logger.info("pigeon client[version:" + VersionUtils.VERSION + "] has been shutdown");
					}
				}
			}
		}
	}

}
