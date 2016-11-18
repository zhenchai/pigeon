package com.dianping.pigeon.monitor.trace;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/17  下午3:15.
 */
public abstract class AbstractAllTraceData<T extends TraceData> {

    protected ConcurrentMap<KeyPair<SourceKey, DestinationKey>, T> traceDatas =
            new ConcurrentHashMap<KeyPair<SourceKey, DestinationKey>, T>();

    public AbstractAllTraceData() {

    }

    public void start(SourceKey srcKey, DestinationKey dstKey) {
        //
    }

    public void complete(SourceKey srcKey, DestinationKey dstKey) {
        //
    }

    public ConcurrentMap<KeyPair<SourceKey, DestinationKey>, T> getTraceDatas() {
        return traceDatas;
    }
}
