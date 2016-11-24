package com.dianping.pigeon.remoting.common.monitor.trace;


import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qi.yin
 *         2016/11/17  下午1:18.
 */
public class InvokerTraceData extends AbstractTraceData {

    public InvokerTraceData() {

    }

    private AtomicLong degradedCount = new AtomicLong();

    private byte callMethod;

    private String region;

    public byte getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getDegradedCount() {
        return degradedCount.get();
    }

    public void incDegradedCount() {
        this.degradedCount.incrementAndGet();
    }

}
