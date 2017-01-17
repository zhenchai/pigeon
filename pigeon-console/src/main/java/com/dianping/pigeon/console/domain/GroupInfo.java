package com.dianping.pigeon.console.domain;

import com.dianping.pigeon.config.ConfigManagerLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenchongze on 17/1/12.
 */
public class GroupInfo {

    private String swimlane = ConfigManagerLoader.getConfigManager().getGroup();

    private Map<String, String> invokerGroupCache = new HashMap<String, String>();

    private Map<String, String> providerGroupCache = new HashMap<String, String>();

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public Map<String, String> getInvokerGroupCache() {
        return invokerGroupCache;
    }

    public void setInvokerGroupCache(Map<String, String> invokerGroupCache) {
        this.invokerGroupCache = invokerGroupCache;
    }

    public Map<String, String> getProviderGroupCache() {
        return providerGroupCache;
    }

    public void setProviderGroupCache(Map<String, String> providerGroupCache) {
        this.providerGroupCache = providerGroupCache;
    }
}
