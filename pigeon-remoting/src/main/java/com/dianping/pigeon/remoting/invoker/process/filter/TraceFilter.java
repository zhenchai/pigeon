package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.monitor.trace.*;
import com.dianping.pigeon.remoting.common.domain.CallMethod;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.MessageType;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
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

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private volatile boolean isTrace = true;

    public TraceFilter() {
        isTrace = configManager.getBooleanValue(Constants.KEY_INVOKER_TRACE_ENABLE, Constants.DEFAULT_INVOKER_TRACE_ENABLE);
        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
    }

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext) throws Throwable {
        if (!isTrace) {
            return handler.handle(invocationContext);
        }

        InvokerConfig config = invocationContext.getInvokerConfig();

        MonitorTransaction transaction = monitor.getCurrentCallTransaction();
        String rootMessage = StringUtils.EMPTY;

        if (transaction != null) {
            rootMessage = transaction.getParentRootMessage();
        }

        SourceKey srcKey = null;

        if (UrlUtils.url(rootMessage)) {
            srcKey = new OtherKey(rootMessage);
        } else {
            srcKey = new ApplicationKey(appName);
        }

//        SourceKey srcKey = new ApplicationKey(appName);

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
                    if (MessageType.isException((byte) response.getMessageType())) {
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

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith(Constants.KEY_INVOKER_TRACE_ENABLE)) {
                try {
                    isTrace = Boolean.valueOf(value);
                } catch (RuntimeException e) {
                }
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onKeyRemoved(String key) {
            // TODO Auto-generated method stub

        }

    }
}
