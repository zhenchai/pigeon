package com.dianping.pigeon.monitor.trace.stats;


/**
 * @author qi.yin
 *         2016/11/17  下午1:18.
 */
public class InvokerTraceData extends AbstractTraceData {

    public InvokerTraceData() {

    }

    private byte callMethod;

    private boolean degraded;

    private String region;

    public byte getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public void setDegraded(boolean degraded) {
        this.degraded = degraded;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
