package com.dianping.pigeon.registry.listener;

import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 17/4/27.
 */
public interface RegistryNotifyListener {

    void onServiceHostChange(String serviceName, List<String[]> hostList, String registryName);

    void onServiceHostChange(String serviceName, List<String[]> toAddHostList, List<String[]> toDelHostList, String registryName);

    void onHostWeightChange(String serverAddress, int weight, String registryName);

    void serverAppChanged(String serverAddress, String app, String registryName);

    void serverVersionChanged(String serverAddress, String version, String registryName);

    void serverProtocolChanged(String serverAddress, Map<String, Boolean> protocolInfoMap, String registryName);

    void serverHeartBeatSupportChanged(String serverAddress, byte heartBeatSupport, String registryName);

}
