package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.util.MapUtils;

/**
 * @author qi.yin
 *         2016/11/17  下午3:12.
 */
public class ProviderTraceRepository extends AbstractTraceRepository<ProviderMonitorData, ProviderTraceData> {

    public void start(ProviderMonitorData monitorData) {
        ProviderTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new PairKey<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                ProviderTraceData.class);

        traceStatsData.incTotalCount();
    }

    public void addData(ProviderMonitorData monitorData) {

        ProviderTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new PairKey<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                ProviderTraceData.class);

        traceStatsData.setCallType(monitorData.getCallType());
        traceStatsData.setSerialize(monitorData.getSerialize());
        traceStatsData.setTimeout(monitorData.getTimeout());
    }

    public void complete(ProviderMonitorData monitorData) {

        ProviderTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new PairKey<SourceKey, DestinationKey>(monitorData.getSrcKey(), monitorData.getDstKey()),
                ProviderTraceData.class);

        long elapsed = System.currentTimeMillis() - monitorData.getStartMillisTime();

        traceStatsData.setElapsed(elapsed);

        if (monitorData.isSuccess()) {
            traceStatsData.incTotalSuccess();
        } else {
            traceStatsData.incTotalFailed();
        }
    }

    @Override
    public AbstractTraceRepository<ProviderMonitorData, ProviderTraceData> createAllTraceData() {
        return new ProviderTraceRepository();
    }
}
