package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.util.MapUtils;

/**
 * @author qi.yin
 *         2016/11/17  下午3:12.
 */
public class InvokerTraceRepository extends AbstractTraceRepository<InvokerMonitorData, InvokerTraceData> {

    public void start(InvokerMonitorData monitorData) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new PairKey<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        traceStatsData.incTotalCount();
    }

    public void addData(InvokerMonitorData monitorData) {

        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new PairKey<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        traceStatsData.setCallMethod(monitorData.getCallMethod());
        traceStatsData.setSerialize(monitorData.getSerialize());
        traceStatsData.setTimeout(monitorData.getTimeout());
        traceStatsData.setRegion(monitorData.getRegion());
    }

    public void complete(InvokerMonitorData monitorData) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new PairKey<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        long elapsed = System.currentTimeMillis() - monitorData.getStartMillisTime();
        traceStatsData.setElapsed(elapsed);

        if (monitorData.isSuccess()) {
            traceStatsData.incTotalSuccess();
        } else {
            traceStatsData.incTotalFailed();
        }
    }

    public void degrade(InvokerMonitorData monitorData) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new PairKey<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        traceStatsData.incDegradedCount();
    }

    @Override
    public AbstractTraceRepository<InvokerMonitorData, InvokerTraceData> createAllTraceData() {
        return new InvokerTraceRepository();
    }
}
