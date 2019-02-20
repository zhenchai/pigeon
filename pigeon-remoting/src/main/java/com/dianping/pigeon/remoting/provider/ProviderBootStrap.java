/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.NetUtils;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderBootStrap {

    private static Logger logger = LoggerLoader.getLogger(ServicePublisher.class);
    static Server httpServer = null;
    static volatile Map<String, Server> serversMap = new HashMap<String, Server>();
    static volatile boolean isInitialized = false;
    static Date startTime = new Date();

    public static Date getStartTime() {
        return startTime;
    }

    public static void init() {
        if (!isInitialized) {
            synchronized (ProviderBootStrap.class) {
                if (!isInitialized) {
                    //工厂初始化，初始化Server端调用链Filter
                    ProviderProcessHandlerFactory.init();
                    //序列化
                    SerializerFactory.init();
                    //从包中加载所有的class到JVM
                    ClassUtils.loadClasses("com.dianping.pigeon");
                    //异步初始化shutdownHook，定义一些在系统关闭时候执行的动作，大部分是做一些清理工作
                    Thread shutdownHook = new Thread(new ShutdownHookListener());
                    shutdownHook.setDaemon(true);
                    shutdownHook.setPriority(Thread.MAX_PRIORITY);
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                    ServerConfig config = new ServerConfig();
                    config.setProtocol(Constants.PROTOCOL_HTTP);
                    //registry配置，定义了和服务操作有关的接口，如注册服务，获取服务地址等
                    RegistryManager.getInstance();
                    /**
                     * server初始化
                     * 读取配置，配置了NettyServer和JettyHttpServer两种，说明一台运行Pigeon的代码的机器会有两个Server实例
                     * 设定dispatcherServlet
                     */
                    List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
                    for (Server server : servers) {
                        if (!server.isStarted()) {
                            if (server.support(config)) {
                                server.start(config);
                                //注册Console信息至注册中心
                                registerConsoleServer(config);
                                //从注册中心拉取或初始化当前应用的服务注册信息
                                //主要用来获取服务名对应的Swimlane（Group）的信息
                                //如果当前应用所在服务器已经在本地设置了Swimlane，则不会再使用注册中心中配置
                                //此处获取的配置在后续的ServicePublisher.publishServer中使用
                                initRegistryConfig(config);

                                httpServer = server;
                                serversMap.put(server.getProtocol() + server.getPort(), server);
                                logger.warn("pigeon " + server + "[version:" + VersionUtils.VERSION + "] has been started");
                            }
                        }
                    }
                    isInitialized = true;
                }
            }
        }
    }

    /**
     * 启动
     * 根据providerConfig获取serverConfig，用于发布
     * @param providerConfig
     * @return
     */
    public static ServerConfig startup(ProviderConfig<?> providerConfig) {
        ServerConfig serverConfig = providerConfig.getServerConfig();
        if (serverConfig == null) {
            throw new IllegalArgumentException("server config is required");
        }
        Server server = serversMap.get(serverConfig.getProtocol() + serverConfig.getPort());
        if (server != null) {
            //server加上providerConfig服务
            server.addService(providerConfig);
            return server.getServerConfig();
        } else {
            /**
             * 1、启动server
             * 2、将服务添加到server上
             */
            synchronized (ProviderBootStrap.class) {
                List<Server> servers = ExtensionLoader.newExtensionList(Server.class);
                for (Server s : servers) {
                    if (!s.isStarted()) {
                        // Protocol支持，default使用NettyServer，http使用JettyHttpServer
                        if (s.support(serverConfig)) {
                            s.start(serverConfig);
                            s.addService(providerConfig);
                            serversMap.put(s.getProtocol() + serverConfig.getPort(), s);
                            logger.warn("pigeon " + s + "[version:" + VersionUtils.VERSION + "] has been started");
                            break;
                        }
                    }
                }
                server = serversMap.get(serverConfig.getProtocol() + serverConfig.getPort());
                if (server != null) {
                    //预启动coreThreads
                    server.getRequestProcessor().getRequestProcessThreadPool().prestartAllCoreThreads();
                    return server.getServerConfig();
                }
                return null;
            }
        }
    }

    public static void shutdown() {
        for (Server server : serversMap.values()) {
            if (server != null) {
                logger.info("start to stop " + server);
                try {
                    unregisterConsoleServer(server.getServerConfig());
                    server.stop();
                } catch (Throwable e) {
                }
                if (logger.isInfoEnabled()) {
                    logger.info(server + " has been shutdown");
                }
            }
        }
        try {
            ProviderProcessHandlerFactory.destroy();
        } catch (Throwable e) {
        }
    }

    public static List<Server> getServers(ProviderConfig<?> providerConfig) {
        List<Server> servers = new ArrayList<Server>();
        servers.add(httpServer);
        String protocol = providerConfig.getServerConfig().getProtocol();
        int port = providerConfig.getServerConfig().getPort();
        servers.add(serversMap.get(protocol + port));

        return servers;
    }

    public static Map<String, Server> getServersMap() {
        return serversMap;
    }

    public static Server getHttpServer() {
        return httpServer;
    }

    private static void initRegistryConfig(ServerConfig config) {
        try {
            RegistryManager.getInstance().initRegistryConfig(config.getIp());
        } catch (RegistryException e) {
            logger.warn("failed to init registry config, set config to blank, please check!", e);
        }
    }

    public static void registerConsoleServer(ServerConfig config) {
        RegistryManager.getInstance().setConsoleAddress(NetUtils.toAddress(config.getIp(), config.getHttpPort()));
    }

    public static void unregisterConsoleServer(ServerConfig config) {
        if (Constants.PROTOCOL_HTTP.equals(config.getProtocol())) {
            RegistryManager.getInstance().unregisterConsoleAddress(NetUtils.toAddress(config.getIp(), config.getHttpPort()));
        }
    }

}
