package com.dianping.pigeon.remoting.common.monitor.trace;


import com.dianping.pigeon.util.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/17  下午3:15.
 */
public abstract class AbstractTraceRepository<M extends MonitorData, T extends AbstractTraceData> implements TraceRepository<M> {

    protected volatile ConcurrentMap<Pair<SourceKey, DestinationKey>, T> traceDatas =
            new ConcurrentHashMap<Pair<SourceKey, DestinationKey>, T>();

    public AbstractTraceRepository() {

    }

    @Override
    public void reset() {
        this.traceDatas = new ConcurrentHashMap<Pair<SourceKey, DestinationKey>, T>();
    }

    @Override
    public TraceRepository copy() {
        AbstractTraceRepository<M,T> allTraceData = createAllTraceData();

        allTraceData.setTraceDatas(traceDatas);
        return allTraceData;
    }

    public abstract AbstractTraceRepository<M,T> createAllTraceData();

    public ConcurrentMap<Pair<SourceKey, DestinationKey>, T> getTraceDatas() {
        return traceDatas;
    }

    public void setTraceDatas(ConcurrentMap<Pair<SourceKey, DestinationKey>, T> traceDatas) {
        this.traceDatas = traceDatas;
    }

}
