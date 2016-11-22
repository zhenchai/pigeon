package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.util.MapUtils;
import com.dianping.pigeon.util.TimeUtils;

/**
 * @author qi.yin
 *         2016/11/17  下午3:12.
 */
public class InvokerAllTraceData extends AbstractAllTraceData<InvokerMonitorData, InvokerTraceData> {

    public void start(InvokerMonitorData monitorData) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        traceStatsData.incTotalCount();
    }

    public void addData(InvokerMonitorData monitorData) {

        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        traceStatsData.setCallMethod(monitorData.getCallMethod());
        traceStatsData.setSerialize(monitorData.getSerialize());
        traceStatsData.setTimeout(monitorData.getTimeout());
        traceStatsData.setRegion(monitorData.getRegion());
    }

    public void complete(InvokerMonitorData monitorData) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        long elapsed = TimeUtils.currentTimeMillis() - monitorData.getStartMillisTime();
        traceStatsData.setElapsed(elapsed);

        if (monitorData.isSuccess()) {
            traceStatsData.incTotalSuccess();
        } else {
            traceStatsData.incTotalFailed();
        }
    }

    public void degrade(InvokerMonitorData monitorData) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                InvokerTraceData.class);

        traceStatsData.incDegradedCount();
    }

    @Override
    public AbstractAllTraceData<InvokerMonitorData, InvokerTraceData> createAllTraceData() {
        return new InvokerAllTraceData();
    }
}
