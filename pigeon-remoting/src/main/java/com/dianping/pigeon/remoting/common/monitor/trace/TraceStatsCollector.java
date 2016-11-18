package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.monitor.trace.ApplicationKey;
import com.dianping.pigeon.monitor.trace.ApplicationTraceData;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.util.TimeUtils;

/**
 * @author qi.yin
 *         2016/11/01  下午4:41.
 */
public class TraceStatsCollector {

    private static final String appName = ConfigManagerLoader.getConfigManager().getAppName();

    private static final ApplicationTraceData statsData = new ApplicationTraceData(appName);

    public static void updateProvideCount(ProviderContext context) {
        InvocationRequest request = context.getRequest();
        statsData.updateProviderTotalCount(new ApplicationKey(request.getApp()), context.getMethodKey());
    }

    public static void addProvideData(ProviderContext context) {
        InvocationRequest request = context.getRequest();
        statsData.addProviderData(new ApplicationKey(request.getApp()), context.getMethodKey(), (byte) request.getCallType(),
                request.getSerialize(), request.getTimeout());
    }

    public static void updateProvideData(ProviderContext context, long startMillis, boolean isSuccess) {
        long elapsed = TimeUtils.currentTimeMillis() - startMillis;

        InvocationRequest request = context.getRequest();
        statsData.updateProviderData(new ApplicationKey(request.getApp()), context.getMethodKey(), elapsed, isSuccess);
    }

    public static void beforeProvide(ProviderContext context) {
        InvocationRequest request = context.getRequest();
        statsData.startProvider(new ApplicationKey(request.getApp()), context.getMethodKey());
    }

    public static void afterProvide(ProviderContext context) {
        InvocationRequest request = context.getRequest();
        statsData.completeProvider(new ApplicationKey(request.getApp()), context.getMethodKey());
    }

    public static void andInvokeData(InvokerContext context) {
        InvokerConfig config = context.getInvokerConfig();
        String methodName = context.getMethodName();
        byte callMethod = config.getCallMethod(methodName);
        int timeout = config.getTimeout(methodName);
        byte serialize = config.getSerialize();
        statsData.addInvokerData(new ApplicationKey(appName), context.getMethodKey(), callMethod, serialize, timeout);
    }

    public static void updateInvokeData(InvokerContext context, long startMillis, boolean isSuccess) {
        long elapsed = TimeUtils.currentTimeMillis() - startMillis;

        statsData.updateInvokerData(new ApplicationKey(appName), context.getMethodKey(), elapsed, isSuccess);
    }

    public static void updateInvokeCount(InvokerContext context) {
        statsData.updateInvokerTotalCount(new ApplicationKey(appName), context.getMethodKey());
    }

    public static void beforeInvoke(InvokerContext context) {
        statsData.startInvoker(new ApplicationKey(appName), context.getMethodKey());
    }

    public static void afterInvoke(InvokerContext context) {
        statsData.completeInvoker(new ApplicationKey(appName), context.getMethodKey());
    }

    public static ApplicationTraceData getStatsData() {
        return statsData;
    }

}
