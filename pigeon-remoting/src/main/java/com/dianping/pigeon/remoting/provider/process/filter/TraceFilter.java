package com.dianping.pigeon.remoting.provider.process.filter;

import com.dianping.pigeon.remoting.common.monitor.trace.ProviderMonitorData;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

/**
 * @author qi.yin
 *         2016/11/14  下午5:45.
 */
public class TraceFilter implements ServiceInvocationFilter<ProviderContext> {

    public TraceFilter() {
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext) throws Throwable {

        InvocationRequest request = invocationContext.getRequest();
        ProviderMonitorData monitorData = (ProviderMonitorData) invocationContext.getMonitorData();

        if (monitorData == null) {
            return handler.handle(invocationContext);
        }

        monitorData.trace();

        monitorData.setCallType((byte) request.getCallType());
        monitorData.setSerialize(request.getSerialize());
        monitorData.setTimeout(request.getTimeout());
        monitorData.add();

        InvocationResponse response = null;

        try {
            response = handler.handle(invocationContext);
        } finally {

            if (invocationContext.getFrameworkError() != null) {
                monitorData.setIsSuccess(false);
            } else {
                monitorData.setIsSuccess(true);
            }

            monitorData.complete();
        }

        return response;

    }

}
