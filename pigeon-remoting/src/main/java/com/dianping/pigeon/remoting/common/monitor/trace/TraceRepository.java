package com.dianping.pigeon.remoting.common.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/21  上午12:36.
 */
public interface TraceRepository<M extends MonitorData> {

    void start(M monitorData);

    void addData(M monitorData);

    void complete(M monitorData);

    void reset();

    TraceRepository copy();
}
