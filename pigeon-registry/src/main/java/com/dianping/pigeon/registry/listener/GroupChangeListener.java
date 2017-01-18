package com.dianping.pigeon.registry.listener;

import com.dianping.pigeon.registry.config.RegistryConfig;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 16/12/22.
 */
public interface GroupChangeListener {

    void onGroupChange(String ip, RegistryConfig oldRegistryConfig, RegistryConfig newRegistryConfig);
}
