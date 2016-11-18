package com.dianping.pigeon.monitor.trace;

import com.dianping.pigeon.util.MapUtils;

/**
 * @author qi.yin
 *         2016/11/17  下午3:12.
 */
public class ProviderAllTraceData extends AbstractAllTraceData<ProviderTraceData> {

    @Override
    public void start(SourceKey srcKey, DestinationKey dstKey) {
        ProviderTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                ProviderTraceData.class);
    }

    public void addData(SourceKey srcKey, DestinationKey dstKey, byte callType, byte serialize, int timeout) {

        ProviderTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                ProviderTraceData.class);

        traceStatsData.setCallType(callType);
        traceStatsData.setSerialize(serialize);
        traceStatsData.setTimeout(timeout);
    }

    public void updateData(SourceKey srcKey, DestinationKey dstKey, long elapsed, boolean isSuccess) {

        ProviderTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                ProviderTraceData.class);

        traceStatsData.setElapsed(elapsed);

        if (isSuccess) {
            traceStatsData.incTotalSuccess();
        } else {
            traceStatsData.incTotalFailed();
        }
    }

    public void updateTotalCount(SourceKey srcKey, DestinationKey dstKey) {

        ProviderTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                ProviderTraceData.class);

        traceStatsData.incTotalCount();
    }
}
