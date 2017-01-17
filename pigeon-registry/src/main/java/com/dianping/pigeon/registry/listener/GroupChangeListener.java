package com.dianping.pigeon.registry.listener;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 16/12/22.
 */
public interface GroupChangeListener {

    void onInvokerGroupChange(String ip, ConcurrentMap oldInvokerGroupCache, ConcurrentMap newInvokerGroupCache);

    void onProviderGroupChange(String ip, ConcurrentMap oldProviderGroupCache, ConcurrentMap newProviderGroupCache);
}
