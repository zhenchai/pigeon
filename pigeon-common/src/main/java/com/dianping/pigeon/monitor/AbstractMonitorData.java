package com.dianping.pigeon.monitor;

import com.dianping.pigeon.util.MapUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/08  下午8:33.
 */
public abstract class AbstractMonitorData extends MonitorData {

    private byte serialize;

    private int timeout;

    public AbstractMonitorData() {

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
