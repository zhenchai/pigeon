package com.dianping.pigeon.remoting.invoker.proxy;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.InvokerBootStrap;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.RouteException;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 15/12/17.
 */
public abstract class AbstractServiceProxy implements ServiceProxy {

    protected final static Map<InvokerConfig<?>, Object> services = new ConcurrentHashMap<InvokerConfig<?>, Object>();
    protected Logger logger = LoggerLoader.getLogger(this.getClass());

    private static final Interner<InvokerConfig<?>> interner = Interners.newWeakInterner();

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;

    @Override
    public void init() {

    }

    @Override
    public <T> T getProxy(InvokerConfig<T> invokerConfig) {
        if (invokerConfig.getServiceInterface() == null) {
            throw new IllegalArgumentException("service interface is required");
        }
        if (StringUtils.isBlank(invokerConfig.getUrl())) {
            invokerConfig.setUrl(ServiceFactory.getServiceUrl(invokerConfig));
        }
        if (!StringUtils.isBlank(invokerConfig.getProtocol())
                && !invokerConfig.getProtocol().equalsIgnoreCase(Constants.PROTOCOL_DEFAULT)) {
            String protocolPrefix = "@" + invokerConfig.getProtocol().toUpperCase() + "@";
            if (!invokerConfig.getUrl().startsWith(protocolPrefix)) {
                invokerConfig.setUrl(protocolPrefix + invokerConfig.getUrl());
            }
        }
        Object service = null;
        service = services.get(invokerConfig);
        if (service == null) {
            synchronized (interner.intern(invokerConfig)) {
                service = services.get(invokerConfig);
                if (service == null) {
                    try {
                        //调用端的初始化,在需要新建一个代理的时候触发
                        InvokerBootStrap.startup();

                        service = SerializerFactory.getSerializer(invokerConfig.getSerialize()).proxyRequest(invokerConfig);
                        if (StringUtils.isNotBlank(invokerConfig.getLoadbalance())) {
                            LoadBalanceManager.register(invokerConfig.getUrl(), invokerConfig.getSuffix(),
                                    invokerConfig.getLoadbalance());
                        }
                    } catch (Throwable t) {
                        throw new RpcException("error while trying to get service:" + invokerConfig, t);
                    }

                    // setup region policy for service
                    try {
                        regionPolicyManager.register(invokerConfig.getUrl(), invokerConfig.getSuffix(),
                                invokerConfig.getRegionPolicy());
                    } catch (Throwable t) {
                        throw new RouteException("error while setup region route policy: " + invokerConfig, t);
                    }

                    try {
                        ClientManager.getInstance().registerClients(invokerConfig);
                    } catch (Throwable t) {
                        logger.warn("error while trying to setup service client:" + invokerConfig, t);
                    }
                    services.put(invokerConfig, service);
                }
            }
        }
        return (T) service;
    }

    @Override
    public Map<InvokerConfig<?>, Object> getAllServiceInvokers() {
        return services;
    }
}
