package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.concurrent.FutureFactory;
import com.dianping.pigeon.remoting.invoker.concurrent.ServiceFutureImpl;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.FaultInjectionManager;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

/**
 * Created by chenchongze on 17/4/18.
 */
public class FaultInjectionFilter extends InvocationInvokeFilter {

    private static final Logger logger = LoggerLoader.getLogger(FaultInjectionFilter.class);

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext) throws Throwable {
        InvocationResponse response = null;
        InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
        int timeout = invokerConfig.getTimeout(invocationContext.getMethodName());

        Integer timeoutThreadLocal = InvokerHelper.getTimeout();
        if (timeoutThreadLocal != null) {
            timeout = timeoutThreadLocal;
        }

        String requestKey = getRequestKey(invocationContext);
        if (FaultInjectionManager.INSTANCE.isEnable(requestKey)) {
            response = doFaultInjection(requestKey, invocationContext, timeout);
        }
        if (response != null) {
            return response;
        }

        String serviceKey = invocationContext.getInvokerConfig().getUrl();
        if (FaultInjectionManager.INSTANCE.isEnable(serviceKey)) {
            response = doFaultInjection(serviceKey, invocationContext, timeout);
        }
        if (response != null) {
            return response;
        }

        if (timeoutThreadLocal != null) {
            InvokerHelper.setTimeout(timeoutThreadLocal);
        }

        return handler.handle(invocationContext);
    }

    private InvocationResponse doFaultInjection(String key, InvokerContext invocationContext, int timeout) {
        InvocationResponse response = null;
        InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
        byte callMethodCode = invokerConfig.getCallMethod(invocationContext.getMethodName());
        CallMethod callMethod = CallMethod.getCallMethod(callMethodCode);

        FaultInjectionManager.FaultInjectionAction faultInjectionAction
                = FaultInjectionManager.INSTANCE.getAction(key);

        switch (callMethod) {
            case SYNC:
            case CALLBACK:
                switch (faultInjectionAction.getType()) {
                    case EXCEPTION:
                        exception(invocationContext);
                        break;
                    case DELAY:
                        if (timeout <= faultInjectionAction.getDelay()) {
                            timeout(timeout, invocationContext);
                        }
                        break;
                    case NONE:
                        break;
                }
                break;
            case FUTURE:
                switch (faultInjectionAction.getType()) {
                    case EXCEPTION:
                        response = exceptionFuture(timeout, invocationContext);
                        break;
                    case DELAY:
                        if (timeout <= faultInjectionAction.getDelay()) {
                            response = timeoutFuture(timeout, invocationContext);
                        }
                        break;
                    case NONE:
                        break;
                }
                break;
            case ONEWAY:
                break;
        }

        return response;
    }

    private String getRequestKey(InvokerContext context) {
        return context.getInvokerConfig().getUrl() + "#" + context.getMethodName();
    }

    private void exception(InvokerContext context) {
        InvocationRequest request = InvocationUtils.newRequest(context);
        request.setCreateMillisTime(System.currentTimeMillis());
        throw new FaultInjectionManager.FaultInjectionException(request.toString());
    }

    private void timeout(int timeout, InvokerContext context) {
        InvocationRequest request = InvocationUtils.newRequest(context);
        request.setCreateMillisTime(System.currentTimeMillis());
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            logger.warn(e.toString());
        }
        throw InvocationUtils.newTimeoutException(
                "request timeout, current time:" + System.currentTimeMillis() + "\r\nrequest:" + request);
    }

    private InvocationResponse exceptionFuture(int timeout, InvokerContext context) {
        InvocationRequest request = InvocationUtils.newRequest(context);
        Exception exception = new FaultInjectionManager.FaultInjectionException(request.toString());
        ServiceFutureImpl future = new ServiceFutureImpl(context, timeout);
        request.setCreateMillisTime(System.currentTimeMillis());
        future.setRequest(request);
        FutureFactory.setFuture(future);
        future.callback(InvokerUtils.createThrowableResponse(exception));
        future.run();
        return InvokerUtils.createFutureResponse(future);
    }

    private InvocationResponse timeoutFuture(int timeout, InvokerContext context) {
        InvocationRequest request = InvocationUtils.newRequest(context);
        ServiceFutureImpl future = new ServiceFutureImpl(context, timeout);
        request.setCreateMillisTime(System.currentTimeMillis());
        future.setRequest(request);
        FutureFactory.setFuture(future);
        future.callback(InvokerUtils.createThrowableResponse(null));
        return InvokerUtils.createFutureResponse(future);
    }

}
