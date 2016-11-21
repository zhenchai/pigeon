package com.dianping.pigeon.monitor.trace.stats;


/**
 * @author qi.yin
 *         2016/11/17  下午1:17.
 */
public class ProviderTraceData extends AbstractTraceData {

    private byte callType;

    public ProviderTraceData() {

    }

    public byte getCallType() {
        return callType;
    }

    public void setCallType(byte callType) {
        this.callType = callType;
    }

}
