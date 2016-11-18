package com.dianping.pigeon.monitor.trace;

import com.dianping.pigeon.util.TimeUtils;

/**
 * @author qi.yin
 *         2016/11/17  下午1:22.
 */
public class ApplicationTraceData {

    private String appName;

    private long startMillis;

    private static transient final ThreadLocal<TraceKey> traceKeys = new ThreadLocal<TraceKey>();

    private InvokerAllTraceData invokerTraceData = new InvokerAllTraceData();

    private ProviderAllTraceData providerTraceData = new ProviderAllTraceData();


    public ApplicationTraceData(String appName) {
        this.appName = appName;
        this.startMillis = TimeUtils.currentTimeMillis();
    }

    public void startProvider(SourceKey srcKey, DestinationKey dstKey) {
        traceKeys.set(dstKey);
        providerTraceData.start(srcKey, dstKey);
    }

    public void completeProvider(SourceKey srcKey, DestinationKey dstKey) {
        traceKeys.remove();
        providerTraceData.complete(srcKey, dstKey);
    }

    public void addProviderData(SourceKey srcKey, DestinationKey dstKey, byte callType, byte serialize, int timeout) {
        providerTraceData.addData(srcKey, dstKey, callType, serialize, timeout);
    }

    public void updateProviderData(SourceKey srcKey, DestinationKey dstKey, long elapsed, boolean isSuccess) {
        providerTraceData.updateData(srcKey, dstKey, elapsed, isSuccess);
    }

    public void updateProviderTotalCount(SourceKey srcKey, DestinationKey dstkey) {
        providerTraceData.updateTotalCount(srcKey, dstkey);
    }

    public void startInvoker(SourceKey srcKey, DestinationKey dstKey) {
        SourceKey sourceKey = (SourceKey) traceKeys.get();

        if (sourceKey != null) {
            srcKey = sourceKey;
        }

        invokerTraceData.start(srcKey, dstKey);
    }

    public void completeInvoker(SourceKey srcKey, DestinationKey dstKey) {
        SourceKey sourceKey = (SourceKey) traceKeys.get();

        if (sourceKey != null) {
            srcKey = sourceKey;
        }
        invokerTraceData.complete(srcKey, dstKey);
    }

    public void addInvokerData(SourceKey srcKey, DestinationKey dstKey, byte callMethod, byte serialize, int timeout) {
        SourceKey sourceKey = (SourceKey) traceKeys.get();

        if (sourceKey != null) {
            srcKey = sourceKey;
        }
        invokerTraceData.addData(srcKey, dstKey, callMethod, serialize, timeout);
    }

    public void updateInvokerData(SourceKey srcKey, DestinationKey dstKey, long elapsed, boolean isSuccess) {
        SourceKey sourceKey = (SourceKey) traceKeys.get();

        if (sourceKey != null) {
            srcKey = sourceKey;
        }
        invokerTraceData.updateData(srcKey, dstKey, elapsed, isSuccess);
    }

    public void updateInvokerTotalCount(SourceKey srcKey, DestinationKey dstKey) {
        SourceKey sourceKey = (SourceKey) traceKeys.get();

        if (sourceKey != null) {
            srcKey = sourceKey;
        }

        invokerTraceData.updateTotalCount(srcKey, dstKey);
    }

    public String getAppName() {
        return appName;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public InvokerAllTraceData getInvokerTraceData() {
        return invokerTraceData;
    }

    public ProviderAllTraceData getProviderTraceData() {
        return providerTraceData;
    }

}
