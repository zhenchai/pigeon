package com.dianping.pigeon.monitor;

/**
 * @author qi.yin
 *         2016/11/16  上午9:39.
 */
public interface InvokerChainable {

    void startInvoker(MethodKey methodKey);

    void completeInvoker(MethodKey methodKey);
}
