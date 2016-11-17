package com.dianping.pigeon.monitor;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/08  下午11:29.
 */
public abstract class AbstractStatsData implements InvokerChainable {

    private final ConcurrentMap<MethodKey, MethodKey> invokerChains = new ConcurrentHashMap<MethodKey, MethodKey>();

    @Override
    public void startInvoker(MethodKey methodKey) {
        invokerChains.putIfAbsent(methodKey, methodKey);
    }

    @Override
    public void completeInvoker(MethodKey methodKey) {

    }

    public ConcurrentMap<MethodKey, MethodKey> getInvokerChains() {
        return invokerChains;
    }


}
