package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.util.TimeUtils;

/**
 * @author qi.yin
 *         2016/11/17  下午1:22.
 */
public class ApplicationTraceData {

    private String appName;

    private long startMillis;

    private static transient final ThreadLocal<TraceKey> traceKeys = new ThreadLocal<TraceKey>();

    private InvokerTraceRepository invokerTraceData = new InvokerTraceRepository();

    private ProviderTraceRepository providerTraceData = new ProviderTraceRepository();


    public ApplicationTraceData(String appName) {
        this.appName = appName;
        this.startMillis = TimeUtils.currentTimeMillis();
    }

    public void start(ProviderMonitorData monitorData) {
        providerTraceData.start(monitorData);
    }

    public void trace(ProviderMonitorData monitorData) {
        traceKeys.set(monitorData.getDstKey());
    }

    public void complete(ProviderMonitorData monitorData) {
        traceKeys.remove();
        providerTraceData.complete(monitorData);
    }

    public void addData(ProviderMonitorData monitorData) {
        providerTraceData.addData(monitorData);
    }

    public void start(InvokerMonitorData monitorData) {
        invokerTraceData.start(monitorData);
    }

    public void complete(InvokerMonitorData monitorData) {
        invokerTraceData.complete(monitorData);
    }

    public void addData(InvokerMonitorData monitorData) {
        invokerTraceData.addData(monitorData);
    }

    public void degrade(InvokerMonitorData monitorData){
        invokerTraceData.degrade(monitorData);
    }

    public void reset() {
        invokerTraceData.reset();
        providerTraceData.reset();
        startMillis = TimeUtils.currentTimeMillis();
    }

    public ApplicationTraceData copy() {
        ApplicationTraceData traceData = new ApplicationTraceData(appName);

        traceData.setInvokerTraceData((InvokerTraceRepository) invokerTraceData.copy());
        traceData.setProviderTraceData((ProviderTraceRepository) providerTraceData.copy());

        return traceData;
    }

    public String getAppName() {
        return appName;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public InvokerTraceRepository getInvokerTraceData() {
        return invokerTraceData;
    }

    public ProviderTraceRepository getProviderTraceData() {
        return providerTraceData;
    }

    public void setInvokerTraceData(InvokerTraceRepository invokerTraceData) {
        this.invokerTraceData = invokerTraceData;
    }

    public void setProviderTraceData(ProviderTraceRepository providerTraceData) {
        this.providerTraceData = providerTraceData;
    }

    public SourceKey getSourceKey() {
        SourceKey sourceKey = (SourceKey) traceKeys.get();
        return sourceKey;
    }

}
