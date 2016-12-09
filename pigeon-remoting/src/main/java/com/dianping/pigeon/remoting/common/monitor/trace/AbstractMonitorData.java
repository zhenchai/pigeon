package com.dianping.pigeon.remoting.common.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/20  下午3:29.
 */
public abstract class AbstractMonitorData implements MonitorData {

    protected final ApplicationTraceRepository traceData;

    protected SourceKey srcKey;

    protected DestinationKey dstKey;

    protected byte serialize;

    protected int timeout;

    protected boolean isSuccess;

    protected long startMillisTime;

    protected boolean completed;

    public AbstractMonitorData(ApplicationTraceRepository traceData) {
        this.traceData = traceData;
    }

    @Override
    public void start() {
        startMillisTime = System.currentTimeMillis();
    }

    @Override
    public void add() {
        //
    }

    @Override
    public void complete() {
        //
    }

    @Override
    public void trace() {
        //
    }

    @Override
    public void degrade() {

    }

    public SourceKey getSrcKey() {
        return srcKey;
    }

    public void setSrcKey(SourceKey srcKey) {
        this.srcKey = srcKey;
    }

    public DestinationKey getDstKey() {
        return dstKey;
    }

    public void setDstKey(DestinationKey dstKey) {
        this.dstKey = dstKey;
    }

    public byte getSerialize() {
        return serialize;
    }

    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public long getStartMillisTime() {
        return startMillisTime;
    }

    public void setStartMillisTime(long startMillisTime) {
        this.startMillisTime = startMillisTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
