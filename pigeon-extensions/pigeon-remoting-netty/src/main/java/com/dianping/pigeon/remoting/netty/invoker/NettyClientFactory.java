/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
/**
 *
 */
package com.dianping.pigeon.remoting.netty.invoker;

import java.util.Map;
import java.util.concurrent.Executors;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientFactory;
import com.dianping.pigeon.remoting.invoker.client.ClientConfig;
import com.dianping.pigeon.remoting.invoker.client.ClientConfigFactory;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;

/**
 *
 */
public class NettyClientFactory implements ClientFactory {

    protected final Logger logger = LoggerLoader.getLogger(getClass());

    private final static ResponseProcessor responseProcessor = ResponseProcessorFactory.selectProcessor();

    private final static ClientConfig clientConfig = ClientConfigFactory.createClientConfig(ConfigManagerLoader.getConfigManager());

    private static volatile org.jboss.netty.channel.ChannelFactory channelFactory = null;

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
        Client client = new NettyClient(clientConfig, getChannelFactory(), connectInfo, responseProcessor);

        logger.info("created netty client: " + connectInfo.getConnect());

        return client;
    }

    public org.jboss.netty.channel.ChannelFactory getChannelFactory() {
        if (channelFactory == null) {

            synchronized (NettyClientFactory.class) {
                if (channelFactory == null) {
                    channelFactory = createChannelFactory();
                }
            }

        }

        return channelFactory;
    }

    private org.jboss.netty.channel.ChannelFactory createChannelFactory() {
        try {
            return new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(
                            new DefaultThreadFactory("Pigeon-Netty-Client-Boss")),
                    clientConfig.getBossThreadPoolCount(),
                    new org.jboss.netty.channel.socket.nio.NioWorkerPool(
                            Executors.newCachedThreadPool(
                                    new DefaultThreadFactory("Pigeon-Netty-Client-Worker")),
                            clientConfig.getWorkerThreadPoolCount()),
                    new HashedWheelTimer(new DefaultThreadFactory("Pigeon-Netty-Client-Timer")));

        } catch (Error e) {
            logger.info("[createChannelFactory] netty version conflict, use runtime version", e);

            return new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(new DefaultThreadFactory("Pigeon-Netty-Client-Boss")),
                    Executors.newCachedThreadPool(new DefaultThreadFactory("Pigeon-Netty-Client-Worker")),
                    clientConfig.getBossThreadPoolCount(),
                    clientConfig.getWorkerThreadPoolCount());
        }

    }
}