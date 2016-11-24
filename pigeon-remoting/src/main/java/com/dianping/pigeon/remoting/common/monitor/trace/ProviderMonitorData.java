package com.dianping.pigeon.remoting.common.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/20  下午3:34.
 */
public class ProviderMonitorData extends AbstractMonitorData {

    private byte callType;

    public ProviderMonitorData(ApplicationTraceRepository traceData, SourceKey srcKey, DestinationKey dstKey) {
        super(traceData);
        this.srcKey = srcKey;
        this.dstKey = dstKey;
    }

    @Override
    public void complete() {
        super.complete();
        this.traceData.complete(this);
    }

    @Override
    public void add() {
        super.add();
        this.traceData.addData(this);
    }

    @Override
    public void start() {
        super.start();
        this.traceData.start(this);
    }

    @Override
    public void trace() {
        this.traceData.trace(this);
    }

    public byte getCallType() {
        return callType;
    }

    public void setCallType(byte callType) {
        this.callType = callType;
    }


}
