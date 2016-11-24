package com.dianping.pigeon.remoting.common.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/20  下午3:32.
 */
public class InvokerMonitorData extends AbstractMonitorData {

    private byte callMethod;

    private String region;

    public byte getCallMethod() {
        return callMethod;
    }

    public InvokerMonitorData(ApplicationTraceRepository traceData, SourceKey srcKey, DestinationKey dstKey) {
        super(traceData);

        SourceKey sourceKey = this.traceData.getSourceKey();

        if (sourceKey != null) {
            this.srcKey = sourceKey;
        } else {
            this.srcKey = srcKey;
        }

        this.dstKey = dstKey;
    }

    @Override
    public void complete() {
        if (!isCompleted()) {
            super.complete();
            setCompleted(true);
            this.traceData.complete(this);
        }
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
    public void degrade() {
        this.traceData.degrade(this);
    }


    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
