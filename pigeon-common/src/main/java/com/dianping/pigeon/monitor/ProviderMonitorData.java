package com.dianping.pigeon.monitor;

/**
 * @author qi.yin
 *         2016/11/08  下午3:08.
 */
public class ProviderMonitorData extends AbstractMonitorData {

    private byte callType;

    public ProviderMonitorData() {

    }

    public byte getCallType() {
        return callType;
    }

    public void setCallType(byte callType) {
        this.callType = callType;
    }
}
