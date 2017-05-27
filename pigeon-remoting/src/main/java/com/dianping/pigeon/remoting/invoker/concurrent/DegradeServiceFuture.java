package com.dianping.pigeon.remoting.invoker.concurrent;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.ApplicationException;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.monitor.trace.InvokerMonitorData;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceDegradedException;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by chenchongze on 17/5/26.
 */
public class DegradeServiceFuture extends CallbackFuture implements Future {

    private static final Logger logger = LoggerLoader.getLogger(DegradeServiceFuture.class);

    private long timeout = Long.MAX_VALUE;

    protected Thread callerThread;

    protected InvokerContext invocationContext;

    public DegradeServiceFuture(InvokerContext invocationContext, long timeout) {
        super();
        this.invocationContext = invocationContext;
        this.timeout = timeout;
        callerThread = Thread.currentThread();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return get(this.timeout);
    }

    public Object get(long timeoutMillis) throws InterruptedException, ExecutionException {
        InvocationResponse response = null;
        String addr = null;
        if (client != null) {
            addr = client.getAddress();
        }
        String callInterface = InvocationUtils.getRemoteCallFullName(invocationContext.getInvokerConfig().getUrl(),
                invocationContext.getMethodName(), invocationContext.getParameterTypes());
        transaction = monitor.createTransaction("PigeonFuture", callInterface, invocationContext);
        if (transaction != null) {
            transaction.setStatusOk();
            transaction.logEvent("PigeonCall.callType", invocationContext.getInvokerConfig().getCallType(), "");
            transaction.logEvent("PigeonCall.serialize", ""
                            + (request == null ? invocationContext.getInvokerConfig().getSerialize() : request.getSerialize()),
                    "");
            transaction.logEvent("PigeonCall.timeout", timeoutMillis + "",
                    invocationContext.getInvokerConfig().getTimeout() + "");
            Client client = invocationContext.getClient();
            if (client != null) {
                String targetApp = RegistryManager.getInstance().getReferencedAppFromCache(client.getAddress());
                transaction.logEvent("PigeonCall.app", targetApp, "");
                transaction.logEvent("PigeonCall.server", client.getAddress(), "");
            }
            invocationContext.getTimeline().add(new InvocationContext.TimePoint(InvocationContext.TimePhase.F, System.currentTimeMillis()));
        }
        try {
            try {
                response = super.waitResponse(timeoutMillis);
                if (transaction != null && response != null) {
                    String size = SizeMonitor.getInstance().getLogSize(response.getSize());
                    if (size != null) {
                        transaction.logEvent("PigeonCall.responseSize", size, "" + response.getSize());
                    }
                    invocationContext.getTimeline().add(new InvocationContext.TimePoint(InvocationContext.TimePhase.R, response.getCreateMillisTime()));
                    invocationContext.getTimeline().add(new InvocationContext.TimePoint(InvocationContext.TimePhase.F, System.currentTimeMillis()));
                }
            } catch (RuntimeException e) {
                // won't happen
                logger.warn("failure degrade in future call type error: " + e.toString());
            }

            setResponseContext(response);

            if (response != null) {
                Object responseReturn = response.getReturn();
                if  (responseReturn instanceof RuntimeException) {
                    throw (RuntimeException) responseReturn;
                } else if (responseReturn instanceof Throwable) {
                    throw new ApplicationException((Throwable) responseReturn);
                } else {
                    return responseReturn;
                }
            } else {
                // won't happen
                throw new ServiceDegradedException(DegradationManager.INSTANCE.getRequestUrl(invocationContext));
            }

        } finally {
            if (transaction != null) {
                DegradationManager.INSTANCE.monitorDegrade(invocationContext, transaction);

                invocationContext.getTimeline().add(new InvocationContext.TimePoint(InvocationContext.TimePhase.E, System.currentTimeMillis()));
                try {
                    transaction.complete();
                } catch (RuntimeException e) {
                    monitor.logMonitorError(e);
                }
            }

            InvokerMonitorData monitorData = (InvokerMonitorData) invocationContext.getMonitorData();
            if (monitorData != null) {
                monitorData.setIsSuccess(true);
                monitorData.complete();
            }
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws java.lang.InterruptedException,
            java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        long timeoutMs = unit.toMillis(timeout);
        try {
            return get(timeoutMs);
        } catch (RequestTimeoutException e) {
            throw new TimeoutException(timeoutMs + "ms timeout:" + e.getMessage());
        } catch (InterruptedException e) {
            throw e;
        }
    }

    protected void processContext() {
        Thread currentThread = Thread.currentThread();
        if (currentThread == callerThread) {
            super.processContext();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (transaction != null) {
            try {
                transaction.complete();
            } catch (RuntimeException e) {
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancel();
    }
}
