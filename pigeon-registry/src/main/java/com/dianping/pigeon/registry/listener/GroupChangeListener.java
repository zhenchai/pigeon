package com.dianping.pigeon.registry.listener;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 16/12/22.
 */
public interface GroupChangeListener {

    void onInvokerGroupChange(String ip, ConcurrentMap<String, String> hostConfigInfoMap);

    void onProviderGroupChange(String ip, ConcurrentMap<String, String> hostConfigInfoMap);
}
