/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.concurrent;

import java.io.Serializable;
import java.util.Map;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.monitor.trace.InvokerMonitorData;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedResponse;
import com.dianping.pigeon.remoting.common.exception.BadResponseException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ContextUtils;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;
import com.dianping.pigeon.remoting.invoker.process.filter.DegradationFilter;

public class ServiceCallbackWrapper implements Callback {

    private static final Logger logger = LoggerLoader.getLogger(ServiceCallbackWrapper.class);

    private static final Monitor monitor = MonitorLoader.getMonitor();

    private InvocationResponse response;

    private InvocationRequest request;

    private Client client;

    private InvocationCallback callback;

    private InvokerContext invocationContext;

    public ServiceCallbackWrapper(InvokerContext invocationContext, InvocationCallback callback) {
        this.invocationContext = invocationContext;
        this.callback = callback;
    }

    @Override
    public void run() {
        InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
        MonitorTransaction transaction = null;
        long currentTime = System.currentTimeMillis();
        String addr = null;
        if (client != null) {
            addr = client.getAddress();
        }
        boolean isSuccess = false;
        String callInterface = InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
                invocationContext.getMethodName(), invocationContext.getParameterTypes());
        try {
            setResponseContext(response);
            if (Constants.MONITOR_ENABLE) {
                transaction = monitor.createTransaction("PigeonCallback", callInterface, invocationContext);
            }
            if (transaction != null) {
                transaction.setStatusOk();
                transaction.logEvent("PigeonCall.callType", invokerConfig.getCallType(), "");
                transaction.logEvent("PigeonCall.serialize", request.getSerialize() + "", "");
                transaction.logEvent("PigeonCall.timeout", invokerConfig.getTimeout() + "", "");

                if (response != null && response.getSize() > 0) {
                    String respSize = SizeMonitor.getInstance().getLogSize(response.getSize());
                    if (respSize != null) {
                        monitor.logEvent("PigeonCall.responseSize", respSize, "" + response.getSize());
                    }
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.R, response.getCreateMillisTime()));
                    invocationContext.getTimeline().add(new TimePoint(TimePhase.R, currentTime));
                }
            }
            if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
                    && request.getCreateMillisTime() + request.getTimeout() < currentTime) {
                StringBuilder msg = new StringBuilder();
                msg.append("request callback timeout:").append(request);
                Exception e = InvocationUtils.newTimeoutException(msg.toString());
                e.setStackTrace(new StackTraceElement[]{});
                InvocationResponse degradedResponse = null;
                if (DegradationManager.INSTANCE.needFailureDegrade(invocationContext)) {
                    try {
                        invocationContext.getDegradeInfo().setFailureDegrade(true);
                        invocationContext.getDegradeInfo().setCause(e);
                        degradedResponse = DegradationFilter.degradeCall(invocationContext);
                    } catch (Throwable t) {
                        logger.warn("failure degrade in callback call type error: " + t.toString());
                    }
                }
                if (degradedResponse == null) {
                    DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
                    ExceptionManager.INSTANCE.logRpcException(addr, invocationContext.getInvokerConfig().getUrl(),
                            invocationContext.getMethodName(), "request callback timeout", e, request, response,
                            transaction);
                }
            }
        } finally {
            try {
                if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
                    completeTransaction(transaction);
                    isSuccess = true;
                    DegradationManager.INSTANCE.addNormalRequest(invocationContext);
                    this.callback.onSuccess(response.getReturn());
                } else if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
                    RpcException e = ExceptionManager.INSTANCE.logRemoteCallException(addr,
                            invocationContext.getInvokerConfig().getUrl(), invocationContext.getMethodName(),
                            "remote call error with callback", request, response, transaction);
                    completeTransaction(transaction);

                    InvocationResponse degradedResponse = null;
                    if (DegradationManager.INSTANCE.needFailureDegrade(invocationContext)) {
                        try {
                            invocationContext.getDegradeInfo().setFailureDegrade(true);
                            invocationContext.getDegradeInfo().setCause(e);
                            degradedResponse = DegradationFilter.degradeCall(invocationContext);
                        } catch (Throwable t) {
                            logger.warn("failure degrade in callback call type error: " + t.toString());
                        }
                    }

                    if (degradedResponse == null) {
                        DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
                        this.callback.onFailure(e);
                    }

                } else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
                    Exception e = ExceptionManager.INSTANCE
                            .logRemoteServiceException("remote service biz error with callback", request, response);
                    completeTransaction(transaction);

                    InvocationResponse degradedResponse = null;
                    if (DegradationManager.INSTANCE.needFailureDegrade(invocationContext)
                            && DegradationManager.INSTANCE.isCustomizedDegradeException(e)) {
                        try {
                            invocationContext.getDegradeInfo().setFailureDegrade(true);
                            invocationContext.getDegradeInfo().setCause(e);
                            degradedResponse = DegradationFilter.degradeCall(invocationContext);
                        } catch (Throwable t) {
                            logger.warn("failure degrade in callback call type error: " + t.toString());
                        }

                        if (degradedResponse == null) { // 没失败降级成功,由于是业务指定降级异常,也得计入失败统计
                            DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
                        }
                    } else {
                        DegradationManager.INSTANCE.addNormalRequest(invocationContext);
                    }

                    if (degradedResponse == null) {
                        this.callback.onFailure(e);
                    }
                } else {
                    RpcException e = new BadResponseException(response.toString());
                    monitor.logError(e);

                    completeTransaction(transaction);
                }
            } catch (Throwable e) {
                logger.error("error while executing service callback", e);
            } finally {
                if (transaction != null) {
                    DegradationManager.INSTANCE.monitorDegrade(invocationContext, transaction);
                }
            }

            InvokerMonitorData monitorData = (InvokerMonitorData) invocationContext.getMonitorData();

            if (monitorData != null) {
                monitorData.setIsSuccess(isSuccess);
                monitorData.complete();
            }
        }
    }

    private void completeTransaction(MonitorTransaction transaction) {
        if (transaction != null) {
            invocationContext.getTimeline().add(new TimePoint(TimePhase.E, System.currentTimeMillis()));
            try {
                transaction.complete();
            } catch (Throwable e) {
                monitor.logMonitorError(e);
            }
        }
    }

    protected void setResponseContext(InvocationResponse response) {
        if (response == null) {
            return;
        }

        if (response instanceof UnifiedResponse) {
            UnifiedResponse _response = (UnifiedResponse) response;
            Map<String, String> responseValues = _response.getLocalContext();
            if (responseValues != null) {
                ContextUtils.setResponseContext((Map) responseValues);
            }
        } else {
            Map<String, Serializable> responseValues = response.getResponseValues();
            if (responseValues != null) {
                ContextUtils.setResponseContext(responseValues);
            }
        }
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public void callback(InvocationResponse response) {
        this.response = response;
    }

    @Override
    public void setRequest(InvocationRequest request) {
        this.request = request;
    }

    @Override
    public void dispose() {

    }

}
