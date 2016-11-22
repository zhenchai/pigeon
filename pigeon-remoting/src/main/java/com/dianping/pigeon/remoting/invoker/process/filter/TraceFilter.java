package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.monitor.trace.InvokerMonitorData;
import com.dianping.pigeon.remoting.common.monitor.trace.ApplicationKey;
import com.dianping.pigeon.remoting.common.monitor.trace.MethodKey;
import com.dianping.pigeon.remoting.common.monitor.trace.Otherkey;
import com.dianping.pigeon.remoting.common.monitor.trace.SourceKey;
import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.MessageType;
import com.dianping.pigeon.remoting.common.monitor.trace.MonitorDataFactory;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import org.apache.commons.lang.StringUtils;

/**
 * @author qi.yin
 *         2016/11/14  下午11:59.
 */
public class TraceFilter extends InvocationInvokeFilter {

    private static final String appName = ConfigManagerLoader.getConfigManager().getAppName();

    private final Monitor monitor = MonitorLoader.getMonitor();

    public TraceFilter() {
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext) throws Throwable {
        InvokerConfig config = invocationContext.getInvokerConfig();

        MonitorTransaction transaction = monitor.getCurrentCallTransaction();
        String rootMessage = StringUtils.EMPTY;

        if (transaction != null) {
            rootMessage = transaction.getParentRootMessage();
        }
        SourceKey srcKey = null;
        if (StringUtils.isNotBlank(rootMessage)) {
            srcKey = new Otherkey(rootMessage);
        } else {
            srcKey = new ApplicationKey(appName);
        }

        InvokerMonitorData monitorData = MonitorDataFactory.newInvokerMonitorData(srcKey,
                new MethodKey(config.getUrl(), invocationContext.getMethodName()));

        invocationContext.setMonitorData(monitorData);
        monitorData.start();

        byte code = config.getCallMethod(invocationContext.getMethodName());
        monitorData.setCallMethod(code);
        monitorData.setSerialize(config.getSerialize());
        monitorData.setTimeout(config.getTimeout(invocationContext.getMethodName()));
        monitorData.add();

        InvocationResponse response = null;
        try {
            response = handler.handle(invocationContext);
        } finally {

            if (response == null) {
                monitorData.setIsSuccess(false);
                monitorData.complete();
            } else {

                if (CallMethod.isSync(code)) {
                    if (MessageType.isServiceException((byte) response.getMessageType()) ||
                            MessageType.isException((byte) response.getMessageType())) {
                        monitorData.setIsSuccess(false);
                    } else {
                        monitorData.setIsSuccess(true);
                    }
                    monitorData.complete();
                } else if (CallMethod.isOneway(code)) {
                    monitorData.setIsSuccess(true);
                    monitorData.complete();
                }
            }

        }

        return response;
    }
}
