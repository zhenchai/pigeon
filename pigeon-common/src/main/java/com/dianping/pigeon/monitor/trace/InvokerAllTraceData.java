package com.dianping.pigeon.monitor.trace;

import com.dianping.pigeon.util.MapUtils;

/**
 * @author qi.yin
 *         2016/11/17  下午3:12.
 */
public class InvokerAllTraceData extends AbstractAllTraceData<InvokerTraceData> {

    @Override
    public void start(SourceKey srcKey, DestinationKey dstKey) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                InvokerTraceData.class);
    }

    public void addData(SourceKey srcKey, DestinationKey dstKey, byte callMethod, byte serialize, int timeout) {

        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                InvokerTraceData.class);

        traceStatsData.setCallMethod(callMethod);
        traceStatsData.setSerialize(serialize);
        traceStatsData.setTimeout(timeout);
    }

    public void updateData(SourceKey srcKey, DestinationKey dstKey, long elapsed, boolean isSuccess) {

        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                InvokerTraceData.class);

        traceStatsData.setElapsed(elapsed);
        if (isSuccess) {
            traceStatsData.incTotalSuccess();
        } else {
            traceStatsData.incTotalFailed();
        }
    }

    public void updateTotalCount(SourceKey srcKey, DestinationKey dstKey) {
        InvokerTraceData traceStatsData = MapUtils.getOrCreate(traceDatas,
                new KeyPair<SourceKey, DestinationKey>(srcKey, dstKey),
                InvokerTraceData.class);

        traceStatsData.incTotalCount();
    }

}
