package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.config.ConfigManagerLoader;

/**
 * @author qi.yin
 *         2016/11/20  下午4:02.
 */
public class MonitorDataFactory {

    private static final String appName = ConfigManagerLoader.getConfigManager().getAppName();

    private static final ApplicationTraceRepository traceData = new ApplicationTraceRepository(appName);

    public static InvokerMonitorData newInvokerMonitorData(SourceKey srcKey, DestinationKey dstKey) {
        InvokerMonitorData monitorData = new InvokerMonitorData(traceData, srcKey, dstKey);
        return monitorData;
    }

    public static ProviderMonitorData newProviderMonitorData(SourceKey srcKey, DestinationKey dstKey) {
        ProviderMonitorData monitorData = new ProviderMonitorData(traceData, srcKey, dstKey);
        return monitorData;
    }

    public static ApplicationTraceRepository getTraceData() {
        return traceData;
    }
}
