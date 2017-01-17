package com.dianping.pigeon.registry.route;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
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

    //private final String UNKNOWN_GROUP = "unknown";
    private final String BLANK_GROUP = "";

    private volatile ConcurrentMap<String, String> invokerGroupCache;
    private volatile ConcurrentMap<String, String> providerGroupCache;

    public Map<String, String> getInvokerGroupCache() {
        return invokerGroupCache != null ? ImmutableMap.copyOf(invokerGroupCache) : new HashMap<String, String>();
    }

    public Map<String, String> getProviderGroupCache() {
        return providerGroupCache != null ? ImmutableMap.copyOf(providerGroupCache) : new HashMap<String, String>();
    }

    public String getInvokerGroup(String serviceName) {
        if (StringUtils.isNotBlank(configManager.getGroup())) { // swimlane is set, do not cache and watch
            return configManager.getGroup();
        }

        //todo 添加监听和缓存
        initInvokerGroupCache();
        String group = null;
        if (invokerGroupCache != null) {
            group = invokerGroupCache.get(serviceName);
        }

        return StringUtils.isBlank(group) ? BLANK_GROUP : group;

    }

    public String getProviderGroup(String serviceName) {
        if (StringUtils.isNotBlank(configManager.getGroup())) { // swimlane is set, do not cache and watch
            return configManager.getGroup();
        }

        //todo 添加监听和缓存
        initProviderGroupCache();
        String group = null;
        if (providerGroupCache != null) {
            group = providerGroupCache.get(serviceName);
        }

        return StringUtils.isBlank(group) ? BLANK_GROUP : group;
    }

    public synchronized void hostConfig4InvokerChanged(String ip, ConcurrentMap hostConfigInfoMap) {
        ConcurrentMap oldCache = invokerGroupCache;
        invokerGroupCache = hostConfigInfoMap;
        RegistryEventListener.hostConfig4InvokerChanged(ip, oldCache, hostConfigInfoMap);
    }

    public synchronized void hostConfig4ProviderChanged(String ip, ConcurrentMap hostConfigInfoMap) {
        ConcurrentMap oldCache = providerGroupCache;
        providerGroupCache = hostConfigInfoMap;
        RegistryEventListener.hostConfig4ProviderChanged(ip, oldCache, hostConfigInfoMap);
    }

    private void initInvokerGroupCache() {
        if (invokerGroupCache == null) {
            synchronized (this) {
                if (invokerGroupCache == null) {
                    try {
                        invokerGroupCache = registryManager.getHostConfig4Invoker();
                    } catch (RegistryException e) {
                        logger.error("error while getting group info for invoker, please check!", e);
                    }
                }
            }
        }
    }

    private void initProviderGroupCache() {
        if (providerGroupCache == null) {
            synchronized (this) {
                if (providerGroupCache == null) {
                    try {
                        providerGroupCache = registryManager.getHostConfig4Provider();
                    } catch (RegistryException e) {
                        logger.error("error while getting group info for provider, please check!", e);
                    }
                }
            }
        }
    }
}
