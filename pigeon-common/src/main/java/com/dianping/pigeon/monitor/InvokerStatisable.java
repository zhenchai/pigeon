package com.dianping.pigeon.monitor;

/**
 * @author qi.yin
 *         2016/11/04  下午3:27.
 */
public interface InvokerStatisable extends Statisable {

    void addInvokerData(MethodKey methodKey, byte callMethod, byte serialize, int timeout);

    void updateInvokerData(MethodKey methodKey, long elapsed, boolean isSuccess);

    void updateInvokerTotalCount(MethodKey methodKey);
}
