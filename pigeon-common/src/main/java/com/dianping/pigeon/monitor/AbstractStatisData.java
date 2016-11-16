package com.dianping.pigeon.monitor;

import com.dianping.pigeon.util.MapUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/08  下午11:29.
 */
public abstract class AbstractStatisData implements InvokerChainable {

    protected static Object PRESENT = new Object();

    private final ConcurrentMap<MethodKey, Object> invokerChains = new ConcurrentHashMap<MethodKey, Object>();

    @Override
    public void startInvoker(MethodKey methodKey) {
        MapUtils.getOrCreate(invokerChains, methodKey, PRESENT);
    }

    @Override
    public void completeInvoker(MethodKey methodKey) {

    }

    public ConcurrentMap<MethodKey, Object> getInvokerChains() {
        return invokerChains;
    }


}
