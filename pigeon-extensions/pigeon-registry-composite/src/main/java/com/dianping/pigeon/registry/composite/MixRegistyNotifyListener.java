package com.dianping.pigeon.registry.composite;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.RegistryNotifyListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.dianping.pigeon.registry.util.Constants;

import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 17/5/4.
 */
public class MixRegistyNotifyListener implements RegistryNotifyListener {

    private final ServiceChangeListener serviceChangeListener = DefaultServiceChangeListener.INSTANCE;
    private final RegistryManager registryManager = RegistryManager.getInstance();
    private static final Logger logger = LoggerLoader.getLogger(MixRegistyNotifyListener.class);
    private final Monitor monitor = MonitorLoader.getMonitor();

    // registryName --> registry
    private static final Map<String, Registry> registrys = MixUtils.getRegistrys();

    private boolean shouldNotify(String serverAddress, String registryName) {
        boolean flag = true; // 非混合模式都要通知

        if (isMixMode()) { // 混合模式
            Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
            if (_registry != null) {
                if (!_registry.getName().equals(registryName)) {
                    flag = false;
                }
            } else {
                Boolean serverActive = MixUtils.getMachineActives().get(serverAddress);
                if (Boolean.TRUE.equals(serverActive)) { // 已激活只接受mns的通知
                    if (!Constants.REGISTRY_MNS_NAME.equals(registryName)) {
                        flag = false;
                    }
                } else if (Boolean.FALSE.equals(serverActive)) { // 未激活只接受curator的通知
                    if (!Constants.REGISTRY_CURATOR_NAME.equals(registryName)) {
                        flag = false;
                    }
                } else {
                    monitor.logEvent("PigeonReg.activeNullError", serverAddress, "");
                }
            }
        }

        return flag;
    }

    private boolean isMixMode() {
        return Constants.REGISTRY_MIX_NAME.equals(registryManager.getRegistry().getName());
    }

    public MixRegistyNotifyListener() {
    }

    @Override
    public void onServiceHostChange(String serviceName, List<String[]> hostList, String registryName) {
        if (isMixMode()) {
            Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
            if (_registry != null) {
                if (_registry.getName().equals(registryName)) {
                    serviceChangeListener.onServiceHostChange(serviceName, hostList);
                }
            } else {
                try {
                    String addresses = registryManager.getRegistry().getServiceAddress(
                            serviceName, registryManager.getGroup(serviceName), RegistryManager.fallbackDefaultGroup);
                    List<String[]> _hostList = MixUtils.getServiceIpPortList(addresses);
                    serviceChangeListener.onServiceHostChange(serviceName, _hostList);
                } catch (Throwable t) {
                    logger.info("failed to service provider for service: " + serviceName);
                }
            }
        } else {
            serviceChangeListener.onServiceHostChange(serviceName, hostList);
        }

    }

    @Override
    public void onServiceHostChange(String serviceName, List<String[]> toAddHostList, List<String[]> toDelHostList, String registryName) {
        if (isMixMode()) {
            Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
            if (_registry != null) {
                if (_registry.getName().equals(registryName)) {
                    serviceChangeListener.onServiceHostChange(serviceName, toAddHostList, toDelHostList);
                }
            } else {
                try {
                    String addresses = registryManager.getRegistry().getServiceAddress(
                            serviceName, registryManager.getGroup(serviceName), RegistryManager.fallbackDefaultGroup);
                    List<String[]> _hostList = MixUtils.getServiceIpPortList(addresses);
                    serviceChangeListener.onServiceHostChange(serviceName, _hostList);
                } catch (Throwable t) {
                    logger.info("failed to service provider for service: " + serviceName);
                }
            }
        } else {
            serviceChangeListener.onServiceHostChange(serviceName, toAddHostList, toDelHostList);
        }

    }

    @Override
    public void onHostWeightChange(String serverAddress, int weight, String registryName) {
        if (shouldNotify(serverAddress, registryName)) {
            serviceChangeListener.onHostWeightChange(serverAddress, weight);
        }
    }

    @Override
    public void serverAppChanged(String serverAddress, String app, String registryName) {
        if (shouldNotify(serverAddress, registryName)) {
            RegistryEventListener.serverAppChanged(serverAddress, app);
        }
    }

    @Override
    public void serverVersionChanged(String serverAddress, String version, String registryName) {
        if (shouldNotify(serverAddress, registryName)) {
            RegistryEventListener.serverVersionChanged(serverAddress, version);
        }
    }

    @Override
    public void serverProtocolChanged(String serverAddress, Map<String, Boolean> protocolInfoMap, String registryName) {
        if (shouldNotify(serverAddress, registryName)) {
            RegistryEventListener.serverProtocolChanged(serverAddress, protocolInfoMap);
        }
    }

    @Override
    public void serverHeartBeatSupportChanged(String serverAddress, byte heartBeatSupport, String registryName) {
        if (shouldNotify(serverAddress, registryName)) {
            RegistryEventListener.serverHeartBeatSupportChanged(serverAddress, heartBeatSupport);
        }
    }
}
