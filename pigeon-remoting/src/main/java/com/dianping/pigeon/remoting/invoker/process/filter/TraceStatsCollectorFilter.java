package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.MessageType;
import com.dianping.pigeon.remoting.common.monitor.trace.TraceStatsCollector;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.util.TimeUtils;

/**
 * @author qi.yin
 *         2016/11/14  下午11:59.
 */
public class TraceStatsCollectorFilter extends InvocationInvokeFilter {

    public TraceStatsCollectorFilter() {
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext) throws Throwable {
        InvokerConfig config = invocationContext.getInvokerConfig();
        long startMillis = TimeUtils.currentTimeMillis();

        TraceStatsCollector.beforeInvoke(invocationContext);
        TraceStatsCollector.andInvokeData(invocationContext);
        TraceStatsCollector.updateInvokeCount(invocationContext);

        InvocationResponse response = null;

        try {
            response = handler.handle(invocationContext);
        } finally {

            if (CallMethod.isSync(config.getCallMethod(invocationContext.getMethodName()))) {
                if (response == null ||
                        (response!=null&& (MessageType.isException((byte) response.getMessageType()) ||
                        MessageType.isServiceException((byte) response.getMessageType())))) {
                    TraceStatsCollector.updateInvokeData(invocationContext, startMillis, false);
                } else  {
                    TraceStatsCollector.updateInvokeData(invocationContext, startMillis, true);
                }
            }

            TraceStatsCollector.afterInvoke(invocationContext);
        }

        return response;
    }
}
