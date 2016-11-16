package com.dianping.pigeon.remoting.invoker.proxy;

import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 15/12/16.
 */
public interface ServiceProxy {

    void init();

    <T> T getProxy(InvokerConfig<T> invokerConfig);

    Map<InvokerConfig<?>, Object> getAllServiceInvokers();
}
