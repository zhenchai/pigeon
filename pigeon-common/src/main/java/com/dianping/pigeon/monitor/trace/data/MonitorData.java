package com.dianping.pigeon.monitor.trace.data;

/**
 * @author qi.yin
 *         2016/11/20  下午3:29.
 */
public interface MonitorData {

    void start();

    void trace();

    void degrade();

    void add();

    void complete();
}
