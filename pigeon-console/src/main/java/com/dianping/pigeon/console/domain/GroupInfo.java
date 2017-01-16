package com.dianping.pigeon.console.domain;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 17/1/12.
 */
public class GroupInfo {

    private ConcurrentMap<String, String> invokerGroupCache = new ConcurrentHashMap<String, String>();

    private ConcurrentMap<String, String> providerGroupCache = new ConcurrentHashMap<String, String>();

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
}
