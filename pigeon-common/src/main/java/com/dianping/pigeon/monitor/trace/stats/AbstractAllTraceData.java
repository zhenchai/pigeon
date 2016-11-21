package com.dianping.pigeon.monitor.trace.stats;

import com.dianping.pigeon.monitor.trace.data.MonitorData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/17  下午3:15.
 */
public abstract class AbstractAllTraceData<M extends MonitorData, T extends AbstractTraceData> implements AllTraceData<M> {

    protected ConcurrentMap<KeyPair<SourceKey, DestinationKey>, T> traceDatas =
            new ConcurrentHashMap<KeyPair<SourceKey, DestinationKey>, T>();

    public AbstractAllTraceData() {

    }

    @Override
    public void reset() {

    }

    public ConcurrentMap<KeyPair<SourceKey, DestinationKey>, T> getTraceDatas() {
        return traceDatas;
    }
}
