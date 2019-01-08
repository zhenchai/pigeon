/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config;

import java.util.Map;

import com.dianping.pigeon.util.ThriftUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ServiceConfigUtils;
import org.springframework.aop.support.AopUtils;

public class ProviderConfig<T> {

    private Class<?> serviceInterface;
    private String url;
    private String version;
    private T service;
    private ServerConfig serverConfig = new ServerConfig();
    private boolean published = false;
    private boolean cancelTimeout = Constants.DEFAULT_TIMEOUT_CANCEL;
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    //是否使用 共享线程池
    private boolean useSharedPool = configManager.getBooleanValue(Constants.KEY_SERVICE_SHARED,
            Constants.DEFAULT_SERVICE_SHARED);
    private Map<String, ProviderMethodConfig> methods;
    /**
     * 线程池：可活动线程数
     */
    private int actives = 0;

    private boolean supported;
    private PoolConfig poolConfig;

    public PoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(PoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public int getActives() {
        return actives;
    }

    public void setActives(int actives) {
        this.actives = actives;
    }

    public Map<String, ProviderMethodConfig> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, ProviderMethodConfig> methods) {
        this.methods = methods;
    }

    public boolean isUseSharedPool() {
        return useSharedPool;
    }

    public void setSharedPool(boolean useSharedPool) {
        this.useSharedPool = useSharedPool;
    }

    public boolean isCancelTimeout() {
        return cancelTimeout;
    }

    public void setCancelTimeout(boolean cancelTimeout) {
        this.cancelTimeout = cancelTimeout;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        if (serverConfig != null) {
            this.serverConfig = serverConfig;
        }
    }

    public ProviderConfig(Class<T> serviceInterface, T service) {
        if (!serviceInterface.isInstance(service)) {
            throw new IllegalArgumentException("Service interface [" + serviceInterface.getName()
                    + "] needs to be implemented by service [" + service + "] of class ["
                    + service.getClass().getName() + "]");
        }
        this.setServiceInterface(serviceInterface);
        this.setService(service);
        supported = ThriftUtils.isSupportedThrift(serviceInterface);
    }

    public ProviderConfig(T service) {
        this((Class<T>) ServiceConfigUtils.getServiceInterface(AopUtils.getTargetClass(service)), service);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        if (url != null) {
            url = url.trim();
        }
        this.url = url;
    }

    public T getService() {
        return service;
    }

    public void setService(T service) {
        this.service = service;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
