/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientSelector;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.util.ThreadPoolUtils;

import com.dianping.pigeon.log.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultClusterListener implements ClusterListener {

    private static final Logger logger = LoggerLoader.getLogger(DefaultClusterListener.class);

    private ConcurrentHashMap<String, List<Client>> serviceClients = new ConcurrentHashMap<String, List<Client>>();

    private ConcurrentHashMap<String, Client> allClients = new ConcurrentHashMap<String, Client>();

    private ScheduledThreadPoolExecutor closeExecutor = new ScheduledThreadPoolExecutor(3, new DefaultThreadFactory(
            "Pigeon-Client-Cache-Close-ThreadPool"));

    private ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    public DefaultClusterListener(ProviderAvailableListener providerAvailableListener) {
        providerAvailableListener.setWorkingClients(serviceClients);
    }

    public void clear() {
        serviceClients = new ConcurrentHashMap<String, List<Client>>();
        allClients = new ConcurrentHashMap<String, Client>();
    }

    public ConcurrentHashMap<String, List<Client>> getServiceClients() {
        return serviceClients;
    }

    public List<Client> getClientList(InvokerConfig<?> invokerConfig) {
        List<Client> clientList = this.serviceClients.get(invokerConfig.getUrl());
        if (CollectionUtils.isEmpty(clientList)) {
            throw new ServiceUnavailableException("no available provider for service:" + invokerConfig.getUrl()
                    + ", group:" + RegistryManager.getInstance().getGroup(invokerConfig.getUrl()) + ", env:"
                    + ConfigManagerLoader.getConfigManager().getEnv());
        }
        String vip = invokerConfig.getVip();
        if (vip != null && vip.startsWith("console:")) {
            Client localClient = allClients.get(configManager.getLocalIp() + vip.substring(vip.indexOf(":")));
            if (localClient != null) {
                return Arrays.asList(localClient);
            }
        }
        return clientList;
    }

    @Override
    public void addConnect(ConnectInfo connectInfo, String serviceName) {
        if (logger.isInfoEnabled()) {
            logger.info("[cluster-listener] add service provider:" + connectInfo);
        }
        Client client = this.allClients.get(connectInfo.getConnect());
        if (clientExisted(connectInfo)) {
            if (client != null) {
                for (List<Client> clientList : serviceClients.values()) {
                    int idx = clientList.indexOf(client);
                    if (idx >= 0 && clientList.get(idx) != client) {
                        closeClientInFuture(client);
                    }
                }
            } else {
                return;
            }
        }
        if (client == null) {
            client = ClientSelector.selectClient(connectInfo);
        }

        if (!this.allClients.containsKey(connectInfo.getConnect())) {
            Client oldClient = this.allClients.putIfAbsent(connectInfo.getConnect(), client);
            if (oldClient != null) {
                client = oldClient;
            }
        } else {
            client = this.allClients.get(connectInfo.getConnect());
        }

        try {
            if (client.isClosed()) {
                client.open();
            } else {
                logger.info("client already connected:" + client);
            }

            for (Entry<String, Integer> sw : connectInfo.getServiceNames().entrySet()) {
                String _serviceName = sw.getKey();
                RegistryEventListener.serverInfoChanged(_serviceName, connectInfo.getConnect());
                List<Client> clientList = this.serviceClients.get(_serviceName);
                if (clientList == null) {
                    clientList = new CopyOnWriteArrayList<Client>();
                    List<Client> oldClientList = this.serviceClients.putIfAbsent(_serviceName, clientList);
                    if (oldClientList != null) {
                        clientList = oldClientList;
                    }
                }
                if (!clientList.contains(client)) {
                    clientList.add(client);
                }
            }
        } catch (Throwable e) {
            logger.error("", e);
        }
    }

    private boolean clientExisted(ConnectInfo connectInfo) {
        for (String serviceName : connectInfo.getServiceNames().keySet()) {
            List<Client> clientList = serviceClients.get(serviceName);
            if (clientList != null) {
                for (Client client : clientList) {
                    if (client != null && client.getAddress().equals(connectInfo.getConnect())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void removeConnect(Client client) {
        if (logger.isInfoEnabled()) {
            logger.info("[cluster-listener] remove service provider:" + client);
        }
        for (String serviceName : this.serviceClients.keySet()) {
            List<Client> clientList = this.serviceClients.get(serviceName);
            if (clientList != null && clientList.contains(client)) {
                clientList.remove(client);
            }
        }
    }

    @Override
    public void doNotUse(String serviceName, String host, int port) {
        if (logger.isInfoEnabled()) {
            logger.info("[cluster-listener] do not use service provider:" + serviceName + ":" + host + ":" + port);
        }
        List<Client> cs = serviceClients.get(serviceName);
        List<Client> newCS = new CopyOnWriteArrayList<Client>();
        if (cs != null && !cs.isEmpty()) {
            newCS.addAll(cs);
        }
        Client clientFound = null;
        for (Client client : cs) {
            if (client != null && client.getHost() != null && client.getHost().equals(host) && client.getPort() == port) {
                newCS.remove(client);
                clientFound = client;
            }
        }
        serviceClients.put(serviceName, newCS);

        // 一个client可能对应多个serviceName，仅当client不被任何serviceName使用时才关闭
        if (clientFound != null) {
            if (!isClientInUse(clientFound)) {
                allClients.remove(clientFound.getAddress());
                RequestQualityManager.INSTANCE.removeClientQualities(clientFound.getAddress());
                closeClientInFuture(clientFound);
            }
        }
    }

    private boolean isClientInUse(Client clientToFind) {
        for (List<Client> clientList : serviceClients.values()) {
            if (clientList.contains(clientToFind)) {
                return true;
            }
        }
        return false;
    }

    private void closeClientInFuture(final Client client) {
        Runnable command = new Runnable() {

            @Override
            public void run() {
                client.close();
                logger.info("close client:" + client.getAddress());
            }

        };
        try {
            closeExecutor.schedule(command, 3000, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            logger.error("error schedule task to close client", e);
        }
    }

    public void destroy() throws Exception {
        ThreadPoolUtils.shutdown(closeExecutor);
    }

    public ConcurrentHashMap<String, Client> getAllClients() {
        return allClients;
    }
}
