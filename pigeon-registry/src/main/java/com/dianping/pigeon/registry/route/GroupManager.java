package com.dianping.pigeon.registry.route;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 16/5/11.
 */
public enum GroupManager {

    INSTANCE;
    GroupManager() {

    }

    private final Logger logger = LoggerLoader.getLogger(this.getClass());
    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private final RegistryManager registryManager = RegistryManager.getInstance();

    private final String UNKNOWN_GROUP = "unknown";
    private final String BLANK_GROUP = "";

    /*private final String localIp = configManager.getLocalIp();
    private final static String GROUP_INVOKER_BASE = "pigeon.group.invoker.";
    private final static String GROUP_PROVIDER_BASE = "pigeon.group.provider.";
    private final static int GROUP_INVOKER_BASE_LENGTH = GROUP_INVOKER_BASE.length();
    private final static int GROUP_PROVIDER_BASE_LENGTH = GROUP_PROVIDER_BASE.length();*/

    private volatile ConcurrentMap<String, String> invokerGroupCache = new ConcurrentHashMap<String, String>();
    private volatile ConcurrentMap<String, String> providerGroupCache = new ConcurrentHashMap<String, String>();

    public ConcurrentMap<String, String> getInvokerGroupCache() {
        return invokerGroupCache;
    }

    public void setInvokerGroupCache(ConcurrentMap<String, String> invokerGroupCache) {
        this.invokerGroupCache = invokerGroupCache;
    }

    public ConcurrentMap<String, String> getProviderGroupCache() {
        return providerGroupCache;
    }

    public void setProviderGroupCache(ConcurrentMap<String, String> providerGroupCache) {
        this.providerGroupCache = providerGroupCache;
    }

    public String getInvokerGroup(String serviceName) {
        if (StringUtils.isNotBlank(configManager.getGroup())) { // swimlane is set, do not cache and watch
            return configManager.getGroup();
        }

        //todo 添加监听和缓存
        String group = invokerGroupCache.get(serviceName);
        if (group == null) {
            synchronized (this) {
                group = invokerGroupCache.get(serviceName);
                if (group == null) {
                    try {
                        setInvokerGroupCache(registryManager.getHostConfig4Invoker());
                        group = invokerGroupCache.get(serviceName);
                        if (group == null) {
                            group = BLANK_GROUP;
                            invokerGroupCache.put(serviceName, group);
                        }
                    } catch (RegistryException e) {
                        logger.warn("failed to get group info for invoker: " + serviceName
                                + ", set group to" + UNKNOWN_GROUP, e);
                        group = UNKNOWN_GROUP;
                    }
                }
            }
        }

        return group;
    }

    public synchronized String getProviderGroup(String serviceName) {
        if (StringUtils.isNotBlank(configManager.getGroup())) { // swimlane is set, do not cache and watch
            return configManager.getGroup();
        }

        //todo 添加监听和缓存
        String group = providerGroupCache.get(serviceName);
        if (group == null) {
            synchronized (this) {
                group = providerGroupCache.get(serviceName);
                if (group == null) {
                    try {
                        setProviderGroupCache(registryManager.getHostConfig4Provider());
                        group = providerGroupCache.get(serviceName);
                        if (group == null) {
                            group = BLANK_GROUP;
                            providerGroupCache.put(serviceName, group);
                        }
                    } catch (RegistryException e) {
                        logger.warn("failed to get group info for provider: " + serviceName
                                + ", set group to" + UNKNOWN_GROUP, e);
                        group = UNKNOWN_GROUP;
                    }
                }
            }
        }

        return group;
    }

    /*private String parseLocalGroup(String groupConfigs) {
        try {
            String[] keyVals = groupConfigs.split(",");
            for(String keyVal : keyVals) {
                String[] ipGroupArray = keyVal.split(":");
                String ip = ipGroupArray[0];
                if(localIp.equals(ip)) {
                    return ipGroupArray[1];
                }
            }
            return BLANK_GROUP;
        } catch (Throwable t) {
            logger.warn("Parse group config error! return unknown group: " + UNKNOWN_GROUP, t);
            return UNKNOWN_GROUP;
        }
    }*/

    public void hostConfig4InvokerChanged(String ip, ConcurrentMap<String, String> hostConfigInfoMap) {
        RegistryEventListener.hostConfig4InvokerChanged(ip, hostConfigInfoMap);
    }

    public void hostConfig4ProviderChanged(String ip, ConcurrentMap<String, String> hostConfigInfoMap) {
        RegistryEventListener.hostConfig4ProviderChanged(ip, hostConfigInfoMap);
    }
}
