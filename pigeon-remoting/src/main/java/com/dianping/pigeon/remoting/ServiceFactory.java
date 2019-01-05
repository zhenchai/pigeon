/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting;

import java.util.List;
import java.util.Map;

import com.dianping.pigeon.remoting.provider.publish.PublishPolicy;
import com.dianping.pigeon.remoting.provider.publish.PublishPolicyLoader;
import com.dianping.pigeon.util.ThriftUtils;
import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.proxy.ServiceProxy;
import com.dianping.pigeon.remoting.invoker.proxy.ServiceProxyLoader;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.publish.ServiceOnlineTask;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public class ServiceFactory {

	private static Logger logger = LoggerLoader.getLogger(ServiceFactory.class);
	private static ServiceProxy serviceProxy = ServiceProxyLoader.getServiceProxy();
	private static PublishPolicy publishPolicy = PublishPolicyLoader.getPublishPolicy();

	static {
		try {
			//服务端初始化
			ProviderBootStrap.init();
		} catch (Throwable t) {
			logger.error("error while initializing service factory:", t);
			System.exit(1);
		}
	}

	public static Map<InvokerConfig<?>, Object> getAllServiceInvokers() {
		return serviceProxy.getAllServiceInvokers();
	}

	public static Map<String, ProviderConfig<?>> getAllServiceProviders() {
		return ServicePublisher.getAllServiceProviders();
	}

	public static <T> String getServiceUrl(Class<T> serviceInterface) {
		if (ThriftUtils.isIDL(serviceInterface)) {
			return serviceInterface.getEnclosingClass().getName();
		} else {
			return serviceInterface.getName();
		}
	}

	public static <T> String getServiceUrl(InvokerConfig<T> invokerConfig) {
		return getServiceUrl(invokerConfig.getServiceInterface());
	}

	public static <T> String getServiceUrl(ProviderConfig<T> providerConfig) {
		return getServiceUrl(providerConfig.getServiceInterface());
	}

	public static <T> T getService(Class<T> serviceInterface) throws RpcException {
		return getService(null, serviceInterface);
	}

	public static <T> T getService(Class<T> serviceInterface, int timeout) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceInterface);
		invokerConfig.setTimeout(timeout);
		return getService(invokerConfig);
	}

	public static <T> T getService(Class<T> serviceInterface, InvocationCallback callback) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceInterface);
		invokerConfig.setCallback(callback);
		return getService(invokerConfig);
	}

	public static <T> T getService(Class<T> serviceInterface, InvocationCallback callback, int timeout)
			throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceInterface);
		invokerConfig.setCallback(callback);
		invokerConfig.setTimeout(timeout);
		return getService(invokerConfig);
	}

	public static <T> T getService(String url, Class<T> serviceInterface) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(url, serviceInterface);
		return getService(invokerConfig);
	}

	public static <T> T getService(String url, Class<T> serviceInterface, int timeout) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(url, serviceInterface);
		invokerConfig.setTimeout(timeout);
		return getService(invokerConfig);
	}

	public static <T> T getService(String url, Class<T> serviceInterface, InvocationCallback callback) throws RpcException {
		return getService(url, serviceInterface, callback, Constants.DEFAULT_INVOKER_TIMEOUT);
	}

	public static <T> T getService(String url, Class<T> serviceInterface, InvocationCallback callback, int timeout)
			throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(url, serviceInterface);
		invokerConfig.setTimeout(timeout);
		invokerConfig.setCallback(callback);
		return getService(invokerConfig);
	}

	public static <T> T getService(InvokerConfig<T> invokerConfig) throws RpcException {
		return serviceProxy.getProxy(invokerConfig);
	}

	public static void startupServer(ServerConfig serverConfig) throws RpcException {
		// ProviderBootStrap.setServerConfig(serverConfig);
		// ProviderBootStrap.startup(serverConfig);
	}

	public static void shutdownServer() throws RpcException {
		ProviderBootStrap.shutdown();
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param serviceInterface
	 * @param service
	 * @throws RpcException
	 */
	public static <T> void addService(Class<T> serviceInterface, T service) throws RpcException {
		addService(null, serviceInterface, service, ServerConfig.DEFAULT_PORT);
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param url
	 * @param serviceInterface
	 * @param service
	 * @throws RpcException
	 */
	public static <T> void addService(String url, Class<T> serviceInterface, T service) throws RpcException {
		addService(url, serviceInterface, service, ServerConfig.DEFAULT_PORT);
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param url
	 * @param serviceInterface
	 * @param service
	 * @param port
	 * @throws RpcException
	 */
	public static <T> void addService(String url, Class<T> serviceInterface, T service, int port) throws RpcException {
		ProviderConfig<T> providerConfig = new ProviderConfig<T>(serviceInterface, service);
		providerConfig.setUrl(url);
		providerConfig.getServerConfig().setPort(port);
		addService(providerConfig);
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void addService(ProviderConfig<T> providerConfig) throws RpcException {
		publishPolicy.doAddService(providerConfig);
	}

	/**
	 * add the services to pigeon and publish these services to registry
	 * 
	 * @param providerConfigList
	 * @throws RpcException
	 */
	public static void addServices(List<ProviderConfig<?>> providerConfigList) throws RpcException {
		if (logger.isInfoEnabled()) {
			logger.info("add services:" + providerConfigList);
		}
		if (providerConfigList != null && !providerConfigList.isEmpty()) {
			for (ProviderConfig<?> providerConfig : providerConfigList) {
				addService(providerConfig);
			}
		}
	}

	/**
	 * publish the service to registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void publishService(ProviderConfig<T> providerConfig) throws RpcException {
		if (StringUtils.isBlank(providerConfig.getUrl())) {
			providerConfig.setUrl(getServiceUrl(providerConfig));
		}
		try {
			ServicePublisher.publishService(providerConfig, true);
		} catch (RegistryException t) {
			throw new RpcException("error while publishing service:" + providerConfig, t);
		}
	}

	/**
	 * publish the service to registry
	 * 
	 * @param url
	 * @throws RpcException
	 */
	public static <T> void publishService(String url) throws RpcException {
		try {
			ServicePublisher.publishService(url);
		} catch (RegistryException t) {
			throw new RpcException("error while publishing service:" + url, t);
		}
	}

	/**
	 * unpublish the service from registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void unpublishService(ProviderConfig<T> providerConfig) throws RpcException {
		try {
			ServicePublisher.unpublishService(providerConfig);
		} catch (RegistryException e) {
			throw new RpcException("error while unpublishing service:" + providerConfig, e);
		}
	}

	/**
	 * unpublish the service from registry
	 * 
	 * @param url
	 * @throws RpcException
	 */
	public static <T> void unpublishService(String url) throws RpcException {
		try {
			ServicePublisher.unpublishService(url);
		} catch (RegistryException e) {
			throw new RpcException("error while unpublishing service:" + url, e);
		}
	}

	/**
	 * unpublish all pigeon services from registry
	 * 
	 * @throws RpcException
	 */
	public static void unpublishAllServices() throws RpcException {
		try {
			ServicePublisher.unpublishAllServices();
		} catch (RegistryException e) {
			throw new RpcException("error while unpublishing all services", e);
		}
	}

	/**
	 * publish all pigeon services to registry
	 * 
	 * @throws RpcException
	 */
	public static void publishAllServices() throws RpcException {
		try {
			ServicePublisher.publishAllServices();
		} catch (RegistryException e) {
			throw new RpcException("error while publishing all services", e);
		}
	}

	/**
	 * remove all pigeon services, including unregister these services from
	 * registry
	 * 
	 * @throws RpcException
	 */
	public static void removeAllServices() throws RpcException {
		try {
			ServicePublisher.removeAllServices();
		} catch (RegistryException e) {
			throw new RpcException("error while removing all services", e);
		}
	}

	/**
	 * remove the service from pigeon, including unregister this service from
	 * registry
	 * 
	 * @param url
	 * @throws RpcException
	 */
	public static void removeService(String url) throws RpcException {
		try {
			ServicePublisher.removeService(url);
		} catch (RegistryException e) {
			throw new RpcException("error while removing service:" + url, e);
		}
	}

	/**
	 * remove the service from pigeon, including unregister this service from
	 * registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void removeService(ProviderConfig<T> providerConfig) throws RpcException {
		removeService(providerConfig.getUrl());
	}

	public static ProviderConfig<?> getServiceConfig(String url) {
		return ServicePublisher.getServiceConfig(url);
	}

	public static void setServerWeight(int weight) throws RegistryException {
		logger.info("set weight:" + weight);
		ServicePublisher.setServerWeight(weight);
	}

	public static void online() throws RegistryException {
		logger.info("online");
		ServicePublisher.setServerWeight(Constants.WEIGHT_DEFAULT);
	}

	public static void offline() throws RegistryException {
		logger.info("offline");
		ServiceOnlineTask.stop();
		ServicePublisher.setServerWeight(0);
	}

	public static boolean isAutoPublish() {
		return ServicePublisher.isAutoPublish();
	}

}
