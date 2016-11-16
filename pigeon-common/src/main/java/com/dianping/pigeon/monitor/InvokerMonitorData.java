package com.dianping.pigeon.monitor;

/**
 * @author qi.yin
 *         2016/11/03  下午8:23.
 */
public class InvokerMonitorData extends AbstractMonitorData {

    public InvokerMonitorData() {

    }

    private byte callMethod;

    public byte getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }
}
