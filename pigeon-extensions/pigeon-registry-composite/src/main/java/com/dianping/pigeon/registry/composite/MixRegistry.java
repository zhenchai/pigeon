package com.dianping.pigeon.registry.composite;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.config.RegistryConfig;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.util.HeartBeatSupport;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 *
 * Created by chenchongze on 17/4/26.
 */
public class MixRegistry implements Registry {

    private final static Logger logger = LoggerLoader.getLogger(MixRegistry.class);
    private final static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private final static Monitor monitor = MonitorLoader.getMonitor();

    private volatile boolean inited = false;

    // registryName --> registry
    private static final Map<String, Registry> registrys = MixUtils.getRegistrys();

    @Override
    public void init() {
        if (!inited) {
            synchronized (this) {
                if (!inited) {
                    try {
                        logger.info("mix registry prefer use mns & curator.");

                        for (Registry registry : ExtensionLoader.getExtensionList(Registry.class)) {
                            String registryName = registry.getName();
                            if (Constants.REGISTRY_MNS_NAME.equals(registryName)
                                    || Constants.REGISTRY_CURATOR_NAME.equals(registryName)) {
                                registry.init();
                                if (registry.isEnable()) {
                                    registrys.put(registry.getName(), registry);
                                }
                            }
                        }

                        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
                        inited = true;
                    } catch (Throwable t) {
                        logger.error("failed to init mix registry...");
                        throw new RuntimeException(t);
                    }
                }
            }
        }
    }

    @Override
    public boolean isEnable() {
        return inited;
    }

    @Override
    public String getName() {
        return Constants.REGISTRY_MIX_NAME;
    }

    @Override
    public String getServiceAddress(String serviceName) throws RegistryException {
        return getServiceAddress(serviceName, Constants.DEFAULT_GROUP);
    }

    @Override
    public String getServiceAddress(String serviceName, String group) throws RegistryException {
        return getServiceAddress(serviceName, group, true);
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup)
            throws RegistryException {
        return getServiceAddress(serviceName, group, fallbackDefaultGroup, true);
    }

    @Override
    public String getServiceAddress(String remoteAppkey, String serviceName, String group, boolean fallbackDefaultGroup)
            throws RegistryException {
        return getServiceAddress(remoteAppkey, serviceName, group, fallbackDefaultGroup, true);
    }

    @Override
    public void registerService(String serviceName, String group, String serviceAddress, int weight)
            throws RegistryException {
        Map<String, Registry> registryMap = getCurrentWriteRegistries();
        for (Registry registry : registryMap.values()) {
            try {
                registry.registerService(serviceName, group, serviceAddress, weight);
            } catch (Throwable t) {
                logger.info("failed to register service to registry: " + registry.getName());
                if (registryMap.size() == 1) {
                    throw new RegistryException(t);
                }
            }
        }
    }

    @Override
    public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
        Map<String, Registry> registryMap = getCurrentWriteRegistries();
        for (Registry registry : registryMap.values()) {
            try {
                registry.unregisterService(serviceName, serviceAddress);
            } catch (Throwable t) {
                logger.info("failed to unregister service to registry: " + registry.getName());
                if (registryMap.size() == 1) {
                    throw new RegistryException(t);
                }
            }
        }
    }

    @Override
    public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
        Map<String, Registry> registryMap = getCurrentWriteRegistries();
        for (Registry registry : registryMap.values()) {
            try {
                registry.unregisterService(serviceName, group, serviceAddress);
            } catch (Throwable t) {
                logger.info("failed to unregister service to registry: " + registry.getName());
                if (registryMap.size() == 1) {
                    throw new RegistryException(t);
                }
            }
        }
    }

    @Override
    public int getServerWeight(String serverAddress, String serviceName) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.getServerWeight(serverAddress, serviceName);
        }

        int weight = Constants.DEFAULT_WEIGHT;
        Boolean serverActive = MixUtils.getMachineActives().get(serverAddress);

        if (Boolean.TRUE.equals(serverActive)) {
            weight = registrys.get(Constants.REGISTRY_MNS_NAME).getServerWeight(serverAddress, serviceName);
        } else if (Boolean.FALSE.equals(serverActive)) {
            weight = registrys.get(Constants.REGISTRY_CURATOR_NAME).getServerWeight(serverAddress, serviceName);
        } else if (configManager.getLocalIp().equals(MixUtils.getIp(serverAddress))) {
            Integer _weight = ServicePublisher.getServerWeight().get(serverAddress);
            if (_weight != null) {
                weight = _weight;
            }
        } else {
            monitor.logEvent("PigeonReg.activeNullError", serverAddress + "#weight", "");
        }

        return weight;
    }

    @Override
    public List<String> getChildren(String key) throws RegistryException {
        throw new RegistryException("unsupported interface in registry: " + getName());
    }

    @Override
    public void setServerWeight(String serverAddress, int weight) throws RegistryException {
        Map<String, Registry> registryMap = getCurrentWriteRegistries();
        for (Registry registry : registryMap.values()) {
            try {
                registry.setServerWeight(serverAddress, weight);
            } catch (Throwable t) {
                logger.info("failed to set weight to registry: " + registry.getName());
                if (registryMap.size() == 1) {
                    throw new RegistryException(t);
                }
            }
        }
    }

    @Override
    public String getServerApp(String serverAddress, String serviceName) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.getServerApp(serverAddress, serviceName);
        }

        String app = "";
        Boolean serverActive = MixUtils.getMachineActives().get(serverAddress);

        if (Boolean.TRUE.equals(serverActive)) {
            app = registrys.get(Constants.REGISTRY_MNS_NAME).getServerApp(serverAddress, serviceName);
        } else if (Boolean.FALSE.equals(serverActive)) {
            app = registrys.get(Constants.REGISTRY_CURATOR_NAME).getServerApp(serverAddress, serviceName);
        } else if (configManager.getLocalIp().equals(MixUtils.getIp(serverAddress))) {
            app = configManager.getAppName();
        } else {
            monitor.logEvent("PigeonReg.activeNullError", serverAddress + "#app", "");
        }

        return app;
    }

    @Override
    public void setServerApp(String serverAddress, String app) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.setServerApp(serverAddress, app);
            } catch (Throwable t) {
                logger.info("failed to set app to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void unregisterServerApp(String serverAddress) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.unregisterServerApp(serverAddress);
            } catch (Throwable t) {
                logger.info("failed to unregister app to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void setServerVersion(String serverAddress, String version) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.setServerVersion(serverAddress, version);
            } catch (Throwable t) {
                logger.info("failed to set version to registry: " + registry.getName());
            }
        }
    }

    @Override
    public String getServerVersion(String serverAddress, String serviceName) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.getServerVersion(serverAddress, serviceName);
        }

        String version = "";
        Boolean serverActive = MixUtils.getMachineActives().get(serverAddress);

        if (Boolean.TRUE.equals(serverActive)) {
            version = registrys.get(Constants.REGISTRY_MNS_NAME).getServerVersion(serverAddress, serviceName);
        } else if (Boolean.FALSE.equals(serverActive)) {
            version = registrys.get(Constants.REGISTRY_CURATOR_NAME).getServerVersion(serverAddress, serviceName);
        } else if (configManager.getLocalIp().equals(MixUtils.getIp(serverAddress))) {
            version = VersionUtils.VERSION;
        } else {
            monitor.logEvent("PigeonReg.activeNullError", serverAddress + "#version", "");
        }

        return version;
    }

    @Override
    public void unregisterServerVersion(String serverAddress) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.unregisterServerVersion(serverAddress);
            } catch (Throwable t) {
                logger.info("failed to unregister version to registry: " + registry.getName());
            }
        }
    }

    @Override
    public String getStatistics() {
        String stats = "";

        for (Registry registry : getCurrentWriteRegistries().values()) {
            stats += registry.getStatistics() + ",";
        }

        return stats;
    }

    @Override
    public byte getServerHeartBeatSupport(String serviceAddress, String serviceName) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.getServerHeartBeatSupport(serviceAddress, serviceName);
        }

        byte support = HeartBeatSupport.BothSupport.getValue();
        Boolean serverActive = MixUtils.getMachineActives().get(serviceAddress);

        if (Boolean.TRUE.equals(serverActive)) {
            support = registrys.get(Constants.REGISTRY_MNS_NAME).getServerHeartBeatSupport(serviceAddress, serviceName);
        } else if (Boolean.FALSE.equals(serverActive)) {
            support = registrys.get(Constants.REGISTRY_CURATOR_NAME).getServerHeartBeatSupport(serviceAddress, serviceName);
        } else if (configManager.getLocalIp().equals(MixUtils.getIp(serviceAddress))) {
            // keep default
        } else {
            monitor.logEvent("PigeonReg.activeNullError", serviceAddress + "#heartBeatSupport", "");
        }

        return support;
    }

    @Override
    public Map<String, Boolean> getServiceProtocols(String serviceAddress, String serviceName) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.getServiceProtocols(serviceAddress, serviceName);
        }

        Map<String, Boolean> serviceProtocols = new HashMap<>();
        Boolean serverActive = MixUtils.getMachineActives().get(serviceAddress);

        if (Boolean.TRUE.equals(serverActive)) {
            serviceProtocols = registrys.get(Constants.REGISTRY_MNS_NAME).getServiceProtocols(serviceAddress, serviceName);
        } else if (Boolean.FALSE.equals(serverActive)) {
            serviceProtocols = registrys.get(Constants.REGISTRY_CURATOR_NAME).getServiceProtocols(serviceAddress, serviceName);
        } else if (configManager.getLocalIp().equals(MixUtils.getIp(serviceAddress))) {
            // keep default
        } else {
            monitor.logEvent("PigeonReg.activeNullError", serviceAddress + "#serviceProtocols", "");
        }

        return serviceProtocols;
    }

    @Override
    public boolean isSupportNewProtocol(String serviceAddress) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.isSupportNewProtocol(serviceAddress);
        }

        boolean support = false;
        Boolean serverActive = MixUtils.getMachineActives().get(serviceAddress);

        if (Boolean.TRUE.equals(serverActive)) {
            support = registrys.get(Constants.REGISTRY_MNS_NAME).isSupportNewProtocol(serviceAddress);
        } else if (Boolean.FALSE.equals(serverActive)) {
            support = registrys.get(Constants.REGISTRY_CURATOR_NAME).isSupportNewProtocol(serviceAddress);
        } else if (configManager.getLocalIp().equals(MixUtils.getIp(serviceAddress))) {
            // keep default
        } else {
            monitor.logEvent("PigeonReg.activeNullError", serviceAddress + "#supportNewProtocol", "");
        }

        return support;
    }

    @Override
    public boolean isSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.isSupportNewProtocol(serviceAddress, serviceName);
        }

        boolean support = false;
        Boolean serverActive = MixUtils.getMachineActives().get(serviceAddress);

        if (Boolean.TRUE.equals(serverActive)) {
            support = registrys.get(Constants.REGISTRY_MNS_NAME).isSupportNewProtocol(serviceAddress, serviceName);
        } else if (Boolean.FALSE.equals(serverActive)) {
            support = registrys.get(Constants.REGISTRY_CURATOR_NAME).isSupportNewProtocol(serviceAddress, serviceName);
        } else if (configManager.getLocalIp().equals(MixUtils.getIp(serviceAddress))) {
            // keep default
        } else {
            monitor.logEvent("PigeonReg.activeNullError", serviceAddress + ":" + serviceName + "#supportNewProtocol", "");
        }

        return support;
    }

    @Override
    public void setSupportNewProtocol(String serviceAddress, String serviceName, boolean support)
            throws RegistryException {
        Map<String, Registry> registryMap = getCurrentWriteRegistries();
        for (Registry registry : registryMap.values()) {
            try {
                registry.setSupportNewProtocol(serviceAddress, serviceName, support);
            } catch (Throwable t) {
                logger.info("failed to set support new protocol to registry: " + registry.getName());
                if (registryMap.size() == 1) {
                    throw new RegistryException(t);
                }
            }
        }
    }

    @Override
    public void unregisterSupportNewProtocol(String serviceAddress, String serviceName, boolean support)
            throws RegistryException {
        Map<String, Registry> registryMap = getCurrentWriteRegistries();
        for (Registry registry : registryMap.values()) {
            try {
                registry.unregisterSupportNewProtocol(serviceAddress, serviceName, support);
            } catch (Throwable t) {
                logger.info("failed to unregister support new protocol to registry: " + registry.getName());
                if (registryMap.size() == 1) {
                    throw new RegistryException(t);
                }
            }
        }
    }

    @Override
    public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.updateHeartBeat(serviceAddress, heartBeatTimeMillis);
            } catch (Throwable t) {
                logger.info("failed to update heartbeat to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void deleteHeartBeat(String serviceAddress) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.deleteHeartBeat(serviceAddress);
            } catch (Throwable t) {
                logger.info("failed to delete heartbeat to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void setServerService(String serviceName, String group, String hosts) throws RegistryException {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.setServerService(serviceName, group, hosts);
            } catch (Throwable t) {
                logger.info("failed to set server service to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void delServerService(String serviceName, String group) throws RegistryException {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.delServerService(serviceName, group);
            } catch (Throwable t) {
                logger.info("failed to delete server service to registry: " + registry.getName());
            }
        }
    }

    @Override
    public void setHostsWeight(String serviceName, String group, String hosts, int weight) throws RegistryException {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.setHostsWeight(serviceName, group, hosts, weight);
            } catch (Throwable t) {
                logger.info("failed to set hosts weight to registry: " + registry.getName());
            }
        }
    }

    @Override
    public String getServiceAddress(String remoteAppkey, String serviceName, String group, boolean fallbackDefaultGroup,
                                    boolean needListener) throws RegistryException {
        return registrys.get(Constants.REGISTRY_MNS_NAME).getServiceAddress(remoteAppkey, serviceName, group
                , fallbackDefaultGroup, needListener);
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup,
                                    boolean needListener) throws RegistryException {
        Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
        if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
            return _registry.getServiceAddress(serviceName, group, fallbackDefaultGroup, needListener);
        }

        String curatorAddress = "";
        String mnsAddress = "";
        for (Registry registry : registrys.values()) {
            try {
                if (Constants.REGISTRY_MNS_NAME.equals(registry.getName())) {
                    mnsAddress = registry.getServiceAddress(serviceName, group, fallbackDefaultGroup, needListener);
                } else if (Constants.REGISTRY_CURATOR_NAME.equals(registry.getName())) {
                    curatorAddress = registry.getServiceAddress(serviceName, group, fallbackDefaultGroup, needListener);
                }
            } catch (Throwable t) {
                logger.info("failed to get service address from registry: " + registry.getName());
            }
        }

        Set<String> curatorAddressSet = new HashSet<>();
        if (StringUtils.isNotBlank(curatorAddress)) {
            Collections.addAll(curatorAddressSet, curatorAddress.split(","));
        }

        Set<String> mnsAddressSet = new HashSet<>();
        if (StringUtils.isNotBlank(mnsAddress)) {
            Collections.addAll(mnsAddressSet, mnsAddress.split(","));
        }

        if (mnsAddressSet.containsAll(curatorAddressSet)) { // 激活mns
            for (String address : mnsAddressSet) {
                Boolean machineActiveCache = MixUtils.getMachineActives().put(address, true);
                if (machineActiveCache != null && Boolean.FALSE.equals(machineActiveCache)) {
                    // 通知machine刷新信息
                    RegistryManager.getInstance().getServiceWeight(address, serviceName, false);
                    RegistryEventListener.serverInfoChanged(serviceName, address);
                    monitor.logEvent("PigeonReg.machineChange", address + "#" + Constants.REGISTRY_MNS_NAME, "");
                }
            }
            Boolean serviceActiveCache = MixUtils.getServiceActives().put(serviceName, true);
            if (serviceActiveCache != null && Boolean.FALSE.equals(serviceActiveCache)) {
                monitor.logEvent("PigeonReg.serviceChange", serviceName + "#" + Constants.REGISTRY_MNS_NAME, "");
            }

            monitor.logEvent("PigeonReg.mnsActive", serviceName, "");
            return mnsAddress;
        } else {
            for (String address : curatorAddressSet) {
                Boolean machineActiveCache = MixUtils.getMachineActives().put(address, false);
                if (machineActiveCache != null && Boolean.TRUE.equals(machineActiveCache)) {
                    // 通知machine刷新信息
                    RegistryManager.getInstance().getServiceWeight(address, serviceName, false);
                    RegistryEventListener.serverInfoChanged(serviceName, address);
                    monitor.logEvent("PigeonReg.machineChange", address + "#" + Constants.REGISTRY_CURATOR_NAME, "");
                }
            }
            Boolean serviceActiveCache = MixUtils.getServiceActives().put(serviceName, false);
            if (serviceActiveCache != null && Boolean.TRUE.equals(serviceActiveCache)) {
                monitor.logEvent("PigeonReg.serviceChange", serviceName + "#" + Constants.REGISTRY_CURATOR_NAME, "");
            }

            monitor.logEvent("PigeonReg.curatorActive", serviceName, "");
            return curatorAddress;
        }
    }


    @Override
    public void setConsoleAddress(String consoleAddress) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.setConsoleAddress(consoleAddress);
            } catch (Throwable t) {
                logger.info("failed to set console address from registry: " + registry.getName(), t);
            }
        }
    }

    @Override
    public void unregisterConsoleAddress(String consoleAddress) {
        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                registry.unregisterConsoleAddress(consoleAddress);
            } catch (Throwable t) {
                logger.info("failed to unregister console address from registry: " + registry.getName(), t);
            }
        }

    }

    @Override
    public List<String> getConsoleAddresses() {
        List<String> consoleAddresses = new ArrayList<>();

        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                List<String> tempAddresses = registry.getConsoleAddresses();
                if (tempAddresses != null && !tempAddresses.isEmpty()) {
                    consoleAddresses.addAll(tempAddresses);
                }
            } catch (Throwable t) {
                logger.info("failed to get console address from registry: " + registry.getName(), t);
            }
        }

        return consoleAddresses;
    }

    @Override
    public RegistryConfig getRegistryConfig(String ip) throws RegistryException {
        RegistryConfig registryConfig;
        List<RegistryConfig> checkList = Lists.newArrayList();

        for (Registry registry : getCurrentWriteRegistries().values()) {
            try {
                checkList.add(registry.getRegistryConfig(ip));
            } catch (Throwable t) {
                logger.info("failed to get registry config from registry: " + registry.getName());
            }
        }

        if (checkList.size() == 0) {
            throw new RegistryException("failed to get registry config");
        }

        registryConfig = checkValueConsistency(checkList, "registry config");

        return registryConfig;
    }

    private <T> T checkValueConsistency(List<T> checkList, String msg) {
        T result = null;

        if (checkList.size() > 0) {
            result = checkList.get(0);
        }

        for (int i = 0; i < checkList.size(); i++) {
            T t = checkList.get(i);

            if (t != null && !t.equals(result)) {
                String errorMsg = msg + " result not same in different registries! index0: " + result + ", index" + i
                        + ": " + t;

                if (configManager.getBooleanValue("pigeon.registry.check.value.consistency.exception", false)) {
                    throw new RuntimeException(errorMsg);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(errorMsg);
                }

                break;
            }
        }

        return result;
    }

    private Map<String, Registry> getCurrentWriteRegistries() {
        if (!MixUtils.isMixForceDoubleWrite()) {
            Registry _registry = registrys.get(MixUtils.getMixReadPrefer());
            if (_registry != null && !Constants.REGISTRY_MIX_NAME.equals(_registry.getName())) {
                Map<String, Registry> registryMap = new HashMap<>();
                registryMap.put(_registry.getName(), _registry);
                return registryMap;
            }
        }

        return registrys;
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onKeyUpdated(String key, String value) {
            try {
                if (key.endsWith(MixUtils.KEY_MIX_MODE_READ_PREFER)) {
                    String _mixReadPrefer = "";
                    switch (value) {
                        case Constants.REGISTRY_MIX_NAME:
                            _mixReadPrefer = Constants.REGISTRY_MIX_NAME;
                            break;
                        case Constants.REGISTRY_MNS_NAME:
                            _mixReadPrefer = Constants.REGISTRY_MNS_NAME;
                            break;
                        case Constants.REGISTRY_CURATOR_NAME:
                            _mixReadPrefer = Constants.REGISTRY_CURATOR_NAME;
                            break;
                    }

                    if (StringUtils.isNotBlank(_mixReadPrefer) && !_mixReadPrefer.equals(MixUtils.getMixReadPrefer())) {
                        MixUtils.setMixReadPrefer(_mixReadPrefer);
                        RegistryEventListener.connectionReconnected();
                    }
                } else if (key.endsWith(MixUtils.KEY_MIX_MODE_FORCE_DOUBLE_WRITE)) {
                    MixUtils.setMixForceDoubleWrite(Boolean.valueOf(value));
                }
            } catch (Throwable t) {
                logger.warn("failed to handle change of key: " + key, t);
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }
}
