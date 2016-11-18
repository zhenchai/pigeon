package com.dianping.pigeon.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/17  下午1:32.
 */
public class AbstractTraceData extends TraceData {

    private byte serialize;

    private int timeout;

    public AbstractTraceData() {

    }

    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    public byte getSerialize() {
        return serialize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
