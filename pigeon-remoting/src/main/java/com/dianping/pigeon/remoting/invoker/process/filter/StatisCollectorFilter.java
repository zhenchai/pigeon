package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.MessageType;
import com.dianping.pigeon.remoting.common.monitor.StatisCollector;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

/**
 * @author qi.yin
 *         2016/11/14  下午11:59.
 */
public class StatisCollectorFilter extends InvocationInvokeFilter {

    public StatisCollectorFilter() {
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext) throws Throwable {
        InvokerConfig config = invocationContext.getInvokerConfig();
        long startMillis = System.currentTimeMillis();

        StatisCollector.beforeInvoke(invocationContext);
        StatisCollector.andInvokeData(invocationContext);
        StatisCollector.updateInvokeCount(invocationContext);

        InvocationResponse response = null;

        try {
            response = handler.handle(invocationContext);
        } finally {

            if (CallMethod.isSync(config.getCallMethod(invocationContext.getMethodName()))) {
                if (response != null && (MessageType.isException((byte) response.getMessageType()) ||
                        MessageType.isServiceException((byte) response.getMessageType()))) {
                    StatisCollector.updateInvokeData(invocationContext, startMillis, false);
                } else {
                    StatisCollector.updateInvokeData(invocationContext, startMillis, true);
                }
            }

            StatisCollector.afterInvoke(invocationContext);
        }

        return response;
    }
}
