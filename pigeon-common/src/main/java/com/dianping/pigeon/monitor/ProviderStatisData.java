package com.dianping.pigeon.monitor;

import com.dianping.pigeon.util.MapUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/03  下午8:25.
 */
public class ProviderStatisData extends AbstractStatisData implements ProviderStatisable {

    private final ProviderMonitorData monitorData = new ProviderMonitorData();

    private final ConcurrentMap<String, ProviderMonitorData> providerDatas = new ConcurrentHashMap<String, ProviderMonitorData>();

    @Override
    public void addProviderData(MethodKey methodKey, String appName, byte callType, byte serialize, int timeout) {
        monitorData.setCallType(callType);
        monitorData.setSerialize(serialize);
        monitorData.setTimeout(timeout);

        ProviderMonitorData data = MapUtils.getOrCreate(
                providerDatas,
                appName,
                ProviderMonitorData.class);

        data.setCallType(callType);
        data.setSerialize(serialize);
        data.setTimeout(timeout);
    }

    @Override
    public void updateProviderData(MethodKey methodKey, String appName, long elapsed, boolean isSuccess) {
        monitorData.setElapsed(elapsed);
        if (isSuccess) {
            monitorData.incTotalSuccess();
        } else {
            monitorData.incTotalFailed();
        }

        ProviderMonitorData data = MapUtils.getOrCreate(
                providerDatas,
                appName,
                ProviderMonitorData.class);

        if (data != null) {
            data.setElapsed(elapsed);
            if (isSuccess) {
                data.incTotalSuccess();
            } else {
                data.incTotalFailed();
            }
        }

    }

    @Override
    public void updateProviderTotalCount(MethodKey methodKey, String appName) {
        monitorData.incTotalCount();
        ProviderMonitorData data = MapUtils.getOrCreate(
                providerDatas,
                appName,
                ProviderMonitorData.class);
        data.incTotalCount();
    }

    public ConcurrentMap<String, ProviderMonitorData> getProviderDatas() {
        return providerDatas;
    }

    public ProviderMonitorData getMonitorData() {
        return monitorData;
    }
}