package com.dianping.pigeon.monitor;

/**
 * @author qi.yin
 *         2016/11/04  下午3:18.
 */
public interface ProviderStatisable extends Statisable {

    void addProviderData(MethodKey methodKey, String appName, byte callType, byte serialize,
                         int timeout);

    void updateProviderData(MethodKey methodKey, String appName, long elapsed, boolean isSuccess);

    void updateProviderTotalCount(MethodKey methodKey, String appName);
}
