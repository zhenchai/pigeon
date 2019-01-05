/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.util.LangUtils;

/**
 * 服务配置，每个提供的接口都有单独的
 */
public class ServerConfig {

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	public static final int DEFAULT_PORT = getDefaultPort();
	public static final int DEFAULT_HTTP_PORT = 4080;
	private int port = configManager.getIntValue("pigeon.server.defaultport", DEFAULT_PORT);
	private int httpPort = configManager.getIntValue("pigeon.httpserver.defaultport", DEFAULT_HTTP_PORT);
	private boolean autoSelectPort = true;
	private boolean enableTest = configManager
			.getBooleanValue(Constants.KEY_TEST_ENABLE, Constants.DEFAULT_TEST_ENABLE);
	private int corePoolSize = Constants.PROVIDER_POOL_CORE_SIZE;
	private int maxPoolSize = Constants.PROVIDER_POOL_MAX_SIZE;
	private int workQueueSize = Constants.PROVIDER_POOL_QUEUE_SIZE;
	private String suffix = configManager.getGroup();
	private String protocol = Constants.PROTOCOL_DEFAULT;
	private String env;
	private String ip;
	private int actualPort = port;

	public ServerConfig() {
	}

	public static int getDefaultPort() {
		int port = 4040;
		try {
			String app = configManager.getAppName();
			if (StringUtils.isNotBlank(app)) {
				port = LangUtils.hash(app, 6000, 2000);
			}
		} catch (Throwable t) {
		}
		return port;
	}

	public int getActualPort() {
		return actualPort;
	}

	public void setActualPort(int actualPort) {
		this.actualPort = actualPort;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isEnableTest() {
		return enableTest;
	}

	public void setEnableTest(boolean enableTest) {
		this.enableTest = enableTest;
	}

	public boolean isAutoSelectPort() {
		return autoSelectPort;
	}

	public void setAutoSelectPort(boolean autoSelectPort) {
		this.autoSelectPort = autoSelectPort;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		if (!StringUtils.isBlank(suffix)) {
			this.suffix = suffix;
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getCorePoolSize() {
		if (corePoolSize <= 0) {
			corePoolSize = 1;
		} else if (corePoolSize > 300) {
			corePoolSize = 300;
		}
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		if (maxPoolSize <= 0) {
			maxPoolSize = 5;
		} else if (maxPoolSize > 1000) {
			maxPoolSize = 1000;
		}
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getWorkQueueSize() {
		if (workQueueSize < 1) {
			workQueueSize = 1;
		} else if (workQueueSize > 50000) {
			workQueueSize = 50000;
		}
		return workQueueSize;
	}

	public void setWorkQueueSize(int workQueueSize) {
		this.workQueueSize = workQueueSize;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
