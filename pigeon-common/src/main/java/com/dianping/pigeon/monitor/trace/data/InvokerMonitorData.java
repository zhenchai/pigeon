package com.dianping.pigeon.monitor.trace.data;

import com.dianping.pigeon.monitor.trace.stats.ApplicationTraceData;
import com.dianping.pigeon.monitor.trace.stats.DestinationKey;
import com.dianping.pigeon.monitor.trace.stats.SourceKey;

/**
 * @author qi.yin
 *         2016/11/20  下午3:32.
 */
public class InvokerMonitorData extends AbstractMonitorData {

    private byte callMethod;

    private String region;

    private boolean isDegraded;

    public byte getCallMethod() {
        return callMethod;
    }

    public InvokerMonitorData(ApplicationTraceData traceData, SourceKey srcKey, DestinationKey dstKey) {
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

    public void setCallMethod(byte callMethod) {
        this.callMethod = callMethod;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isDegraded() {
        return isDegraded;
    }

    public void setIsDegraded(boolean isDegraded) {
        this.isDegraded = isDegraded;
    }
}
