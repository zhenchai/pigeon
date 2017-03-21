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
                    ProviderProcessHandlerFactory.init();
                    SerializerFactory.init();
                    ClassUtils.loadClasses("com.dianping.pigeon");
                    Thread shutdownHook = new Thread(new ShutdownHookListener());
                    shutdownHook.setDaemon(true);
                    shutdownHook.setPriority(Thread.MAX_PRIORITY);
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                    ServerConfig config = new ServerConfig();
                    config.setProtocol(Constants.PROTOCOL_HTTP);
                    RegistryManager.getInstance();
                    List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
                    for (Server server : servers) {
                        if (!server.isStarted()) {
                            if (server.support(config)) {
                                server.start(config);
                                registerConsoleServer(config);
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

    public static ServerConfig startup(ProviderConfig<?> providerConfig) {
        ServerConfig serverConfig = providerConfig.getServerConfig();
        if (serverConfig == null) {
            throw new IllegalArgumentException("server config is required");
        }
        Server server = serversMap.get(serverConfig.getProtocol() + serverConfig.getPort());
        if (server != null) {
            server.addService(providerConfig);
            return server.getServerConfig();
        } else {
            synchronized (ProviderBootStrap.class) {
                List<Server> servers = ExtensionLoader.newExtensionList(Server.class);
                for (Server s : servers) {
                    if (!s.isStarted()) {
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
