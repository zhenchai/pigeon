package com.dianping.pigeon.remoting.common.monitor;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.monitor.trace.data.InvokerMonitorData;
import com.dianping.pigeon.monitor.trace.data.ProviderMonitorData;
import com.dianping.pigeon.monitor.trace.stats.ApplicationTraceData;
import com.dianping.pigeon.monitor.trace.stats.DestinationKey;
import com.dianping.pigeon.monitor.trace.stats.SourceKey;

/**
 * @author qi.yin
 *         2016/11/20  下午4:02.
 */
public class MonitorDataFactory {

    private static final String appName = ConfigManagerLoader.getConfigManager().getAppName();

    private static final ApplicationTraceData traceData = new ApplicationTraceData(appName);

    public static InvokerMonitorData newInvokerMonitorData(SourceKey srcKey, DestinationKey dstKey) {
        InvokerMonitorData monitorData = new InvokerMonitorData(traceData, srcKey, dstKey);
        return monitorData;
    }

    public static ProviderMonitorData newProviderMonitorData(SourceKey srcKey, DestinationKey dstKey) {
        ProviderMonitorData monitorData = new ProviderMonitorData(traceData, srcKey, dstKey);
        return monitorData;
    }

    public static ApplicationTraceData getTraceData() {
        return traceData;
    }
}
