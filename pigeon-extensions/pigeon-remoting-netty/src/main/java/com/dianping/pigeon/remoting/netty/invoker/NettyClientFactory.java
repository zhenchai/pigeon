/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
/**
 *
 */
package com.dianping.pigeon.remoting.netty.invoker;

import java.util.Map;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientFactory;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;

/**
 *
 */
public class NettyClientFactory implements ClientFactory {

	public static final int CONNECT_TIMEOUT = ConfigManagerLoader.getConfigManager()
			.getIntValue("pigeon.netty.connecttimeout", 2000);

	public static final int WRITE_BUFFER_HIGH_WATER = ConfigManagerLoader.getConfigManager()
			.getIntValue("pigeon.channel.writebuff.high", 35 * 1024 * 1024);

	public static final int WRITE_BUFFER_LOW_WATER = ConfigManagerLoader.getConfigManager()
			.getIntValue("pigeon.channel.writebuff.low", 25 * 1024 * 1024);

	private final static ResponseProcessor responseProcessor = ResponseProcessorFactory.selectProcessor();

	@Override
	public boolean support(ConnectInfo connectInfo) {
		Map<String, Integer> serviceNames = connectInfo.getServiceNames();
		if (!CollectionUtils.isEmpty(serviceNames)) {
			String name = serviceNames.keySet().iterator().next();
			if (name.startsWith("@")) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Client createClient(ConnectInfo connectInfo) {
		return new NettyClient(connectInfo, CONNECT_TIMEOUT, WRITE_BUFFER_HIGH_WATER, WRITE_BUFFER_LOW_WATER,
				Constants.getChannelPoolInitialSize(), Constants.getChannelPoolNormalSize(),
				Constants.getChannelPoolMaxActive(), Constants.getChannelPoolMaxWait(),
				Constants.getChannelPoolTimeBetweenCheckerMillis(), responseProcessor,
				Constants.getInvokerHeartbeatEnable(), Constants.getInvokerHeartbeatTimeout(),
				Constants.getDefaultInvokerClientDeadthreshold(), Constants.getInvokerHeartbeatInterval());
	}

}
