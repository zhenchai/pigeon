package com.dianping.pigeon.remoting.provider.process.filter;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.MessageType;
import com.dianping.pigeon.remoting.common.monitor.trace.TraceStatsCollector;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.util.TimeUtils;

/**
 * @author qi.yin
 *         2016/11/14  下午5:45.
 */
public class TraceStatsCollectorFilter implements ServiceInvocationFilter<ProviderContext> {

    public TraceStatsCollectorFilter() {
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext) throws Throwable {

        long startMillis = TimeUtils.currentTimeMillis();
        TraceStatsCollector.beforeProvide(invocationContext);
        TraceStatsCollector.addProvideData(invocationContext);
        InvocationResponse response = null;

        try {
            response = handler.handle(invocationContext);
        } finally {
            //frame exception
            if (invocationContext.getFrameworkError() != null ||
                    (response != null && MessageType.isException((byte) response.getMessageType()))) {
                TraceStatsCollector.updateProvideData(invocationContext, startMillis, false);
            } else {
                //reply manual or normal return
                TraceStatsCollector.updateProvideData(invocationContext, startMillis, true);
            }

            TraceStatsCollector.afterProvide(invocationContext);
        }

        return response;
    }
}
