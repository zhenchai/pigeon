package com.dianping.pigeon.remoting.provider;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.provider.process.RequestProcessorFactory;
import com.dianping.pigeon.remoting.provider.publish.ServiceChangeListener;
import com.dianping.pigeon.remoting.provider.publish.ServiceChangeListenerContainer;
import com.dianping.pigeon.util.FileUtils;
import com.dianping.pigeon.util.NetUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

public abstract class AbstractServer implements Server {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());
	RequestProcessor requestProcessor = null;
	ServerConfig serverConfig = null;

	public abstract void doStart(ServerConfig serverConfig);

	public abstract void doStop();

	public abstract <T> void doAddService(ProviderConfig<T> providerConfig);

	public abstract <T> void doRemoveService(ProviderConfig<T> providerConfig);

	public void start(ServerConfig serverConfig) {
		if (logger.isInfoEnabled()) {
			logger.info("server config:" + serverConfig);
		}
		requestProcessor = RequestProcessorFactory.selectProcessor();
		//实际的启动Server的处理，由其实现类完成
		doStart(serverConfig);
		if (requestProcessor != null) {
			//启动请求处理的线程池
			requestProcessor.start(serverConfig);
		}
		this.serverConfig = serverConfig;
	}

	public void stop() {
		doStop();
		if (requestProcessor != null) {
			requestProcessor.stop();
		}
	}

	/**
	 * 设置RequestThreadPoolProcessor的线程池
	 * Service级别线程池和Method级别线程池
	 * @param providerConfig
	 * @param <T>
	 */
	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) {
		//RequestThreadPoolProcessor
		requestProcessor.addService(providerConfig);
		// TODO: 2019/1/8 nettyServer or jettyHttpServer添加服务
		doAddService(providerConfig);
		// TODO: 2019/1/8 服务改变的监听器
		List<ServiceChangeListener> listeners = ServiceChangeListenerContainer.getListeners();
		for (ServiceChangeListener listener : listeners) {
			listener.notifyServiceAdded(providerConfig);
		}
	}

	@Override
	public <T> void removeService(ProviderConfig<T> providerConfig) {
		requestProcessor.removeService(providerConfig);
		doRemoveService(providerConfig);
		List<ServiceChangeListener> listeners = ServiceChangeListenerContainer.getListeners();
		for (ServiceChangeListener listener : listeners) {
			listener.notifyServiceRemoved(providerConfig);
		}
	}

	public RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

	@Override
	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	@Override
	public Future<InvocationResponse> processRequest(InvocationRequest request, ProviderContext providerContext) {
		return requestProcessor.processRequest(request, providerContext);
	}

	public int getAvailablePort(int port) {
		int lastPort = port;
		String filePath = LoggerLoader.LOG_ROOT + "/pigeon-port.conf";
		File file = new File(filePath);
		Properties properties = null;
		String key = null;
		try {
			key = this.getClass().getResource("/").getPath() + port;
			if (file.exists()) {
				try {
					properties = FileUtils.readFile(new FileInputStream(file));
					String strLastPort = properties.getProperty(key);
					if (StringUtils.isNotBlank(strLastPort)) {
						lastPort = Integer.parseInt(strLastPort);
					}
				} catch (Exception e) {
				}
			}
		} catch (RuntimeException e) {
		}
		lastPort = NetUtils.getAvailablePort(lastPort);
		if (properties == null) {
			properties = new Properties();
		}
		if (key != null) {
			properties.put(key, lastPort);
		}
		try {
			FileUtils.writeFile(file, properties);
		} catch (IOException e) {
		}
		return lastPort;
	}

}
