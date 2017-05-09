package com.dianping.pigeon.registry.listener;

import com.dianping.pigeon.registry.Registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 17/4/27.
 */
public class DefaultRegistryNotifyListener implements RegistryNotifyListener {

    private final ServiceChangeListener serviceChangeListener = DefaultServiceChangeListener.INSTANCE;

    public DefaultRegistryNotifyListener() {}

    @Override
    public void onServiceHostChange(String serviceName, List<String[]> hostList, String registryName) {
        serviceChangeListener.onServiceHostChange(serviceName, hostList);
    }

    @Override
    public void onServiceHostChange(String serviceName, List<String[]> toAddHostList, List<String[]> toDelHostList, String registryName) {
        serviceChangeListener.onServiceHostChange(serviceName, toAddHostList, toDelHostList);
    }

    @Override
    public void onHostWeightChange(String serverAddress, int weight, String registryName) {
        serviceChangeListener.onHostWeightChange(serverAddress, weight);
    }

    @Override
    public void serverAppChanged(String serverAddress, String app, String registryName) {
        RegistryEventListener.serverAppChanged(serverAddress, app);
    }

    @Override
    public void serverVersionChanged(String serverAddress, String version, String registryName) {
        RegistryEventListener.serverVersionChanged(serverAddress, version);
    }

    @Override
    public void serverProtocolChanged(String serverAddress, Map<String, Boolean> protocolInfoMap, String registryName) {
        RegistryEventListener.serverProtocolChanged(serverAddress, protocolInfoMap);
    }

    @Override
    public void serverHeartBeatSupportChanged(String serverAddress, byte heartBeatSupport, String registryName) {
        RegistryEventListener.serverHeartBeatSupportChanged(serverAddress, heartBeatSupport);
    }
}
