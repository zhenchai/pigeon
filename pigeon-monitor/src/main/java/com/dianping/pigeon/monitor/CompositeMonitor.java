package com.dianping.pigeon.monitor;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanjun on 2017/4/12.
 */
public class CompositeMonitor implements Monitor {

    private static final Logger logger = LoggerLoader.getLogger(CompositeMonitor.class);

    private List<Monitor> monitorList;
    private ThreadLocal<MonitorTransaction> tlCallTransition = new ThreadLocal<>();
    private ThreadLocal<MonitorTransaction> tlServiceTransition = new ThreadLocal<>();


    public CompositeMonitor(List<Monitor> monitorList) {
        if (monitorList == null) {
            monitorList = new ArrayList<>();
        }
        this.monitorList = monitorList;
    }

    @Override
    public void init() {
        for (Monitor monitor : monitorList) {
            monitor.init();
        }
    }

    @Override
    public void logError(String msg, Throwable t) {
        for (Monitor monitor : monitorList) {
            monitor.logError(msg, t);
        }
    }

    @Override
    public void logError(Throwable t) {
        for (Monitor monitor : monitorList) {
            monitor.logError(t);
        }
    }

    @Override
    public void logEvent(String name, String event, String desc) {
        for (Monitor monitor : monitorList) {
            monitor.logEvent(name, event, desc);
        }
    }

    @Override
    public void logMonitorError(Throwable t) {
        for (Monitor monitor : monitorList) {
            monitor.logMonitorError(t);
        }
    }

    @Override
    public MonitorTransaction createTransaction(String name, String uri, Object invocationContext) {
        List<MonitorTransaction> monitorTransactionList = new ArrayList<>(monitorList.size());
        for (Monitor monitor : monitorList) {
            MonitorTransaction transaction = monitor.createTransaction(name, uri, invocationContext);
            if (transaction != null) {
                monitorTransactionList.add(transaction);
            }
        }
        return new CompositeTransaction(monitorTransactionList);
    }

    @Override
    public MonitorTransaction getCurrentCallTransaction() {
        return tlCallTransition.get();
    }

    @Override
    public void setCurrentCallTransaction(MonitorTransaction transaction) {
        tlCallTransition.set(transaction);
    }

    @Override
    public void clearCallTransaction() {
        tlCallTransition.remove();
    }

    @Override
    public MonitorTransaction getCurrentServiceTransaction() {
        return tlServiceTransition.get();
    }

    @Override
    public void setCurrentServiceTransaction(MonitorTransaction transaction) {
        tlServiceTransition.set(transaction);
    }

    @Override
    public void clearServiceTransaction() {
        tlServiceTransition.remove();
    }
}
