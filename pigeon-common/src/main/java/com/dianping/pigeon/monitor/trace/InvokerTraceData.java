package com.dianping.pigeon.monitor.trace;


/**
 * @author qi.yin
 *         2016/11/17  下午1:18.
 */
public class InvokerTraceData extends AbstractTraceData {

    public InvokerTraceData() {

    }

    private byte callMethod;

    public byte getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }
}
