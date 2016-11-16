package com.dianping.pigeon.remoting.provider.process.filter;

import com.dianping.pigeon.monitor.MethodKey;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.MessageType;
import com.dianping.pigeon.remoting.common.monitor.StatisCollector;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

/**
 * @author qi.yin
 *         2016/11/14  下午5:45.
 */
public class StatisCollectorFilter implements ServiceInvocationFilter<ProviderContext> {

    public StatisCollectorFilter() {
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext) throws Throwable {

        long startMillis = System.currentTimeMillis();
        StatisCollector.beforeProvide(invocationContext);
        StatisCollector.addProvideData(invocationContext);
        InvocationResponse response = null;

        try {
            response = handler.handle(invocationContext);
        } finally {
            //frame exception
            if (invocationContext.getFrameworkError() != null ||
                    (response != null && MessageType.isException((byte) response.getMessageType()))) {
                StatisCollector.updateProvideData(invocationContext, startMillis, false);
            } else {
                //reply manual or normal return
                StatisCollector.updateProvideData(invocationContext, startMillis, true);
            }

            StatisCollector.afterProvide(invocationContext);
        }

        return response;
    }
}
