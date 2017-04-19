package com.dianping.pigeon.monitor;

import java.util.List;

/**
 * Created by zhanjun on 2017/4/12.
 */
public class CompositeTransaction implements MonitorTransaction {

    private List<MonitorTransaction> monitorTransactionList;

    public CompositeTransaction(List<MonitorTransaction> monitorTransactionList) {
        this.monitorTransactionList = monitorTransactionList;
    }

    @Override
    public void setStatusError(Throwable t) {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.setStatusError(t);
        }
    }

    @Override
    public void complete() {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.complete();
        }
    }

    @Override
    public void complete(long startTime) {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.complete(startTime);
        }
    }

    @Override
    public void setStatusOk() {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.setStatusOk();
        }
    }

    @Override
    public void addData(String name, Object data) {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.addData(name, data);
        }
    }

    @Override
    public void readMonitorContext(String serverDomain) {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.readMonitorContext(serverDomain);
        }
    }

    @Override
    public void writeMonitorContext() {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.writeMonitorContext();
        }
    }

    @Override
    public void logEvent(String name, String event, String desc) {
        for (MonitorTransaction transaction : monitorTransactionList) {
            transaction.logEvent(name, event, desc);
        }
    }

    @Override
    public String getParentRootMessage() {
        return null;
    }
}
