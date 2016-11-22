package com.dianping.pigeon.remoting.common.monitor.trace;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/17  下午3:15.
 */
public abstract class AbstractAllTraceData<M extends MonitorData, T extends AbstractTraceData> implements AllTraceData<M> {

    protected volatile ConcurrentMap<KeyPair<SourceKey, DestinationKey>, T> traceDatas =
            new ConcurrentHashMap<KeyPair<SourceKey, DestinationKey>, T>();

    public AbstractAllTraceData() {

    }

    @Override
    public void reset() {
        this.traceDatas = new ConcurrentHashMap<KeyPair<SourceKey, DestinationKey>, T>();
    }

    @Override
    public AllTraceData copy() {
        AbstractAllTraceData<M,T> allTraceData = createAllTraceData();

        allTraceData.setTraceDatas(traceDatas);
        return allTraceData;
    }

    public abstract AbstractAllTraceData<M,T> createAllTraceData();

    public ConcurrentMap<KeyPair<SourceKey, DestinationKey>, T> getTraceDatas() {
        return traceDatas;
    }

    public void setTraceDatas(ConcurrentMap<KeyPair<SourceKey, DestinationKey>, T> traceDatas) {
        this.traceDatas = traceDatas;
    }

}
