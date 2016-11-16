package com.dianping.pigeon.monitor;

/**
 * @author qi.yin
 *         2016/11/16  上午9:40.
 */
public interface ProviderChainable {

    void startProvider(MethodKey methodKey);

    void completeProvider(MethodKey methodKey);
}
