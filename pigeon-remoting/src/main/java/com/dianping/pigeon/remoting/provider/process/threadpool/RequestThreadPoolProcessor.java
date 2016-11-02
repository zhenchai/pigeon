/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.threadpool;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.spring.PoolBean;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ProviderMethodConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.process.filter.GatewayProcessFilter;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodCache;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.CollectionUtils;

public class RequestThreadPoolProcessor extends AbstractRequestProcessor {

    private static final Logger logger = LoggerLoader.getLogger(RequestThreadPoolProcessor.class);

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static final String poolStrategy = configManager.getStringValue(
            "pigeon.provider.pool.strategy", "shared");

    private static ThreadPool sharedRequestProcessThreadPool = null;

    private static final int SLOW_POOL_CORESIZE = configManager.getIntValue(
            "pigeon.provider.pool.slow.coresize", 30);

    private static final int SLOW_POOL_MAXSIZE = configManager.getIntValue(
            "pigeon.provider.pool.slow.maxsize", 200);

    private static final int SLOW_POOL_QUEUESIZE = configManager.getIntValue(
            "pigeon.provider.pool.slow.queuesize", 500);

    private static ThreadPool slowRequestProcessThreadPool = new DefaultThreadPool(
            "Pigeon-Server-Slow-Request-Processor", SLOW_POOL_CORESIZE, SLOW_POOL_MAXSIZE,
            new LinkedBlockingQueue<Runnable>(SLOW_POOL_QUEUESIZE));

    private ThreadPool requestProcessThreadPool = null;

    private static ConcurrentHashMap<String, ThreadPool> methodThreadPools = new ConcurrentHashMap<String, ThreadPool>();

    private static ConcurrentHashMap<String, ThreadPool> serviceThreadPools = new ConcurrentHashMap<String, ThreadPool>();

    private static int DEFAULT_POOL_ACTIVES = configManager.getIntValue(
            "pigeon.provider.pool.actives", 60);

    private static float DEFAULT_POOL_RATIO_CORE = configManager.getFloatValue(
            "pigeon.provider.pool.ratio.coresize", 3f);

    private static float cancelRatio = configManager.getFloatValue(
            "pigeon.timeout.cancelratio", 1f);

    private static Map<String, String> methodPoolConfigKeys = new HashMap<String, String>();

    private static String sharedPoolCoreSizeKey = null;

    private static String sharedPoolMaxSizeKey = null;

    private static String sharedPoolQueueSizeKey = null;

    private static boolean enableSlowPool = configManager.getBooleanValue(
            "pigeon.provider.pool.slow.enable", true);

    private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();
    private static final String KEY_PROVIDER_POOL_CONFIG_ENABLE = "pigeon.provider.pool.config.enable";
    private static final String KEY_PROVIDER_POOL_CONFIG = "pigeon.provider.pool.config";
    private static final String KEY_PROVIDER_POOL_API_CONFIG = "pigeon.provider.pool.api.config";
    // poolName --> poolBean
    private volatile static Map<String, PoolBean> poolNameMapping = Maps.newConcurrentMap();
    // url or url#method --> poolName
    private volatile static Map<String, String> apiPoolConfigMapping = Maps.newConcurrentMap();

    // spring url or url#method --> poolBean
    private static ConcurrentHashMap<String, PoolBean> springApiPoolBeanMapping = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, PoolBean> springPoolNameMapping = new ConcurrentHashMap<>();
    private static Map<String, String> springPoolBeanCoreSizeKeys = Maps.newHashMap();
    private static Map<String, String> springPoolBeanMaxSizeKeys = Maps.newHashMap();
    private static Map<String, String> springPoolBeanQueueSizeKeys = Maps.newHashMap();

    static {
        if (configManager.getBooleanValue(KEY_PROVIDER_POOL_CONFIG_ENABLE, false)) {
            try {
                init();
            } catch (Throwable t) {
                throw new RuntimeException("failed to init pool config! please check!", t);
            }
        }
    }

    public RequestThreadPoolProcessor(ServerConfig serverConfig) {
        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
        if ("server".equals(poolStrategy)) {
            requestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-"
                    + serverConfig.getProtocol() + "-" + serverConfig.getActualPort(), serverConfig.getCorePoolSize(),
                    serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(serverConfig.getWorkQueueSize()));
        } else {
            sharedRequestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor",
                    serverConfig.getCorePoolSize(), serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(
                    serverConfig.getWorkQueueSize()));
            requestProcessThreadPool = sharedRequestProcessThreadPool;
        }
    }

    private static void init() throws Throwable {
        String poolConfig = configManager.getStringValue(KEY_PROVIDER_POOL_CONFIG, "");
        refreshPoolConfig(poolConfig);

        String apiPoolConfig = configManager.getStringValue(KEY_PROVIDER_POOL_API_CONFIG, "");
        refreshApiPoolConfig(apiPoolConfig);
    }

    private static synchronized void refreshPoolConfig(String poolConfig) throws Throwable {
        if (StringUtils.isNotBlank(poolConfig)) {
            PoolBean[] poolBeen = (PoolBean[])jacksonSerializer.toObject(PoolBean[].class, poolConfig);
            Map<String, PoolBean> _poolNameMapping = Maps.newConcurrentMap();

            for (PoolBean poolBean : poolBeen) {
                if(poolBean.checkNotNull()) {
                    _poolNameMapping.put(poolBean.getPoolName(), poolBean);
                } else {//报异常,保持原状
                    throw new RuntimeException("pool config error! please check: " + poolBean);
                }
            }

            if(_poolNameMapping.size() != poolBeen.length) {//报异常,保持原状
                throw new RuntimeException("conflict pool name exists, please check!");
            } else {
                List<PoolBean> poolBeenToClose = Lists.newLinkedList();
                for (Map.Entry<String, PoolBean> oldPoolBeanEntry : poolNameMapping.entrySet()) {
                    String oldPoolName = oldPoolBeanEntry.getKey();
                    PoolBean oldPoolBean = oldPoolBeanEntry.getValue();
                    PoolBean _poolBean = _poolNameMapping.get(oldPoolName);
                    if (oldPoolBean.equals(_poolBean)) {
                        _poolNameMapping.remove(oldPoolName);
                        _poolNameMapping.put(oldPoolName, oldPoolBean);
                    } else {
                        poolBeenToClose.add(oldPoolBean);
                    }
                }
                poolNameMapping = _poolNameMapping;
                for (PoolBean poolBeanToClose : poolBeenToClose) {
                    poolBeanToClose.closeThreadPool();
                }
            }
        }
    }

    private static synchronized void refreshApiPoolConfig(String servicePoolConfig) throws Throwable {
        if (StringUtils.isNotBlank(servicePoolConfig)) {
            Map<String, String> _servicePoolConfigMapping = (Map) jacksonSerializer.toObject(Map.class, servicePoolConfig);
            apiPoolConfigMapping = new ConcurrentHashMap<>(_servicePoolConfigMapping);
        }
    }

    public void doStop() {
    }

    public Future<InvocationResponse> doProcessRequest(final InvocationRequest request,
                                                       final ProviderContext providerContext) {
        requestContextMap.put(request, providerContext);
        Callable<InvocationResponse> requestExecutor = new Callable<InvocationResponse>() {

            @Override
            public InvocationResponse call() throws Exception {
                providerContext.getTimeline().add(new TimePoint(TimePhase.T));
                try {
                    ServiceInvocationHandler invocationHandler = ProviderProcessHandlerFactory
                            .selectInvocationHandler(providerContext.getRequest().getMessageType());
                    if (invocationHandler != null) {
                        providerContext.setThread(Thread.currentThread());
                        return invocationHandler.handle(providerContext);
                    }
                } catch (Throwable t) {
                    logger.error("Process request failed with invocation handler, you should never be here.", t);
                } finally {
                    requestContextMap.remove(request);
                }
                return null;
            }
        };
        final ThreadPool pool = selectThreadPool(request);
        // MonitorTransaction transaction =
        // monitor.createTransaction("PigeonRequestSubmit", "",
        // providerContext);
        // transaction.setStatusOk();
        try {
            checkRequest(pool, request);
            providerContext.getTimeline().add(new TimePoint(TimePhase.T));
            return pool.submit(requestExecutor);
        } catch (RejectedExecutionException e) {
            // transaction.setStatusError(e);
            requestContextMap.remove(request);
            throw new RejectedException(getProcessorStatistics(request), e);
        }
        // finally {
        // transaction.complete();
        // }
    }

    private void checkRequest(final ThreadPool pool, final InvocationRequest request) {
        GatewayProcessFilter.checkRequest(request);
    }

    private ThreadPool selectThreadPool(final InvocationRequest request) {
        ThreadPool pool = null;
        PoolBean poolBean = null;
        String serviceKey = request.getServiceName();
        String methodKey = serviceKey + "#" + request.getMethodName();

        // spring配置方式
        poolBean = springApiPoolBeanMapping.get(methodKey);
        if (poolBean != null) {
            pool = poolBean.getRefreshedThreadPool();
        } else {
            poolBean = springApiPoolBeanMapping.get(serviceKey);
            if (poolBean != null) {
                pool = poolBean.getRefreshedThreadPool();
            }
        }

        if (pool == null && !CollectionUtils.isEmpty(methodThreadPools)) {
            pool = methodThreadPools.get(methodKey);
        }
        if (pool == null && !CollectionUtils.isEmpty(serviceThreadPools)) {
            pool = serviceThreadPools.get(serviceKey);
        }

        // 配置中心方式
        if (pool == null && configManager.getBooleanValue(KEY_PROVIDER_POOL_CONFIG_ENABLE, false)
                && !CollectionUtils.isEmpty(apiPoolConfigMapping)) {
            String poolName = apiPoolConfigMapping.get(methodKey);
            if (StringUtils.isNotBlank(poolName)) { // 方法级别
                poolBean = poolNameMapping.get(poolName);
                if(poolBean != null) {
                    pool = poolBean.getRefreshedThreadPool();
                }
            } else { // 服务级别
                poolName = apiPoolConfigMapping.get(serviceKey);
                if (StringUtils.isNotBlank(poolName)) {
                    poolBean = poolNameMapping.get(poolName);
                    if(poolBean != null) {
                        pool = poolBean.getRefreshedThreadPool();
                    }
                }
            }
        }

        // 默认方式
        if (pool == null) {
            if (enableSlowPool && requestTimeoutListener.isSlowRequest(request)) {
                pool = slowRequestProcessThreadPool;
            } else {
                if ("server".equals(poolStrategy)) {
                    pool = requestProcessThreadPool;
                } else {
                    pool = sharedRequestProcessThreadPool;
                }
            }
        }

        return pool;
    }

    @Override
    public String getProcessorStatistics() {
        StringBuilder stats = new StringBuilder();

        if ("server".equals(poolStrategy)) {
            stats.append("[server=").append(getThreadPoolStatistics(requestProcessThreadPool)).append("]");
        } else {
            stats.append("[shared=").append(getThreadPoolStatistics(sharedRequestProcessThreadPool)).append("]");
        }
        stats.append("[slow=").append(getThreadPoolStatistics(slowRequestProcessThreadPool)).append("]");

        if (!CollectionUtils.isEmpty(springApiPoolBeanMapping)) {
            for (String key : springApiPoolBeanMapping.keySet()) {
                stats.append(",[").append(key).append("=")
                        .append(getThreadPoolStatistics(springApiPoolBeanMapping.get(key).getRefreshedThreadPool()))
                        .append("]");
            }
        }

        if (!CollectionUtils.isEmpty(serviceThreadPools)) {
            for (String key : serviceThreadPools.keySet()) {
                stats.append(",[").append(key).append("=").append(getThreadPoolStatistics(serviceThreadPools.get(key)))
                        .append("]");
            }
        }
        if (!CollectionUtils.isEmpty(methodThreadPools)) {
            for (String key : methodThreadPools.keySet()) {
                stats.append(",[").append(key).append("=").append(getThreadPoolStatistics(methodThreadPools.get(key)))
                        .append("]");
            }
        }

        if (!CollectionUtils.isEmpty(apiPoolConfigMapping)) {
            for (Map.Entry<String, String> entry : apiPoolConfigMapping.entrySet()) {
                String api = entry.getKey();
                String poolName = entry.getValue();
                PoolBean poolBean = poolNameMapping.get(poolName);
                if (poolBean != null) {
                    stats.append(",[").append(api).append("=")
                            .append(getThreadPoolStatistics(poolBean.getRefreshedThreadPool())).append("]");
                }
            }
        }

        stats.append(GatewayProcessFilter.getStatistics());
        return stats.toString();
    }

    private boolean needStandalonePool(ProviderConfig<?> providerConfig) {
        return !providerConfig.isUseSharedPool() || "method".equals(poolStrategy);
    }

    @Override
    public synchronized <T> void addService(ProviderConfig<T> providerConfig) {
        String url = providerConfig.getUrl();
        Map<String, ProviderMethodConfig> methodConfigs = providerConfig.getMethods();
        ServiceMethodCache methodCache = ServiceMethodFactory.getServiceMethodCache(url);
        Set<String> methodNames = methodCache.getMethodMap().keySet();
        if (needStandalonePool(providerConfig)) {
            if (providerConfig.getPoolBean() != null) { // 服务的poolBean方式,支持方法的fallback
                springApiPoolBeanMapping.putIfAbsent(url, providerConfig.getPoolBean());
                springPoolNameMapping.putIfAbsent(providerConfig.getPoolBean().getPoolName(), providerConfig.getPoolBean());
            } else if (providerConfig.getActives() > 0 && CollectionUtils.isEmpty(methodConfigs)) { // 服务的actives方式,不支持方法的fallback,不支持动态修改
                ThreadPool pool = serviceThreadPools.get(url);
                if (pool == null) {
                    int actives = providerConfig.getActives();
                    int coreSize = (int) (actives / DEFAULT_POOL_RATIO_CORE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_CORE)
                            : actives;
                    int maxSize = actives;
                    int queueSize = actives;
                    pool = new DefaultThreadPool("Pigeon-Server-Request-Processor-service", coreSize, maxSize,
                            new LinkedBlockingQueue<Runnable>(queueSize));
                    serviceThreadPools.putIfAbsent(url, pool);
                }
            }

            if (!CollectionUtils.isEmpty(methodConfigs)) { // 方法级设置方式
                for (String name : methodNames) {
                    String key = url + "#" + name;
                    ProviderMethodConfig methodConfig = methodConfigs.get(name);
                    ThreadPool pool = methodThreadPools.get(key);
                    if (methodConfig != null) {
                        if (methodConfig.getPoolBean() != null) { // 方法poolBean方式
                            springApiPoolBeanMapping.putIfAbsent(key, methodConfig.getPoolBean());
                            springPoolNameMapping.putIfAbsent(methodConfig.getPoolBean().getPoolName(), methodConfig.getPoolBean());
                        } else if (pool == null) { // 方法actives方式
                            int actives = DEFAULT_POOL_ACTIVES;
                            if (methodConfig.getActives() > 0) {
                                actives = methodConfig.getActives();
                            }
                            int coreSize = (int) (actives / DEFAULT_POOL_RATIO_CORE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_CORE)
                                    : actives;
                            int maxSize = actives;
                            int queueSize = actives;
                            pool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method", coreSize, maxSize,
                                    new LinkedBlockingQueue<Runnable>(queueSize));
                            methodThreadPools.putIfAbsent(key, pool);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getProcessorStatistics(InvocationRequest request) {
        ThreadPool pool = selectThreadPool(request);
        return getThreadPoolStatistics(pool);
    }

    private String getThreadPoolStatistics(ThreadPool pool) {
        if (pool == null) {
            return null;
        }
        ThreadPoolExecutor e = pool.getExecutor();
        String stats = String.format(
                "request pool size:%d(active:%d,core:%d,max:%d,largest:%d),task count:%d(completed:%d),queue size:%d,queue remaining:%d",
                e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.getQueue().size(), e.getQueue().remainingCapacity());
        return stats;
    }

    @Override
    public synchronized <T> void removeService(ProviderConfig<T> providerConfig) {
        if (needStandalonePool(providerConfig)) {

            Set<String> toRemoveKeys = new HashSet<String>();
            for (String key : springApiPoolBeanMapping.keySet()) {
                if (key.startsWith(providerConfig.getUrl() + "#") || key.equals(providerConfig.getUrl())) {
                    toRemoveKeys.add(key);
                }
            }
            for (String key : toRemoveKeys) {
                springApiPoolBeanMapping.remove(key);
            }

            toRemoveKeys = new HashSet<String>();
            for (String key : methodThreadPools.keySet()) {
                if (key.startsWith(providerConfig.getUrl() + "#")) {
                    toRemoveKeys.add(key);
                }
            }
            for (String key : toRemoveKeys) {
                ThreadPool pool = methodThreadPools.remove(key);
                if (pool != null) {
                    pool.getExecutor().shutdown();
                }
            }
            ThreadPool pool = serviceThreadPools.remove(providerConfig.getUrl());
            if (pool != null) {
                pool.getExecutor().shutdown();
            }
        }
    }

    @Override
    public boolean needCancelRequest(InvocationRequest request) {
        ThreadPool pool = selectThreadPool(request);
        return pool.getExecutor().getPoolSize() >= pool.getExecutor().getMaximumPoolSize() * cancelRatio;
    }

    @Override
    public ThreadPool getRequestProcessThreadPool() {
        ThreadPool pool;

        if ("server".equals(poolStrategy)) {
            pool = requestProcessThreadPool;
        } else {
            pool = sharedRequestProcessThreadPool;
        }

        return pool;
    }

    @Override
    public void doStart() {

    }

    public static Map<String, String> getMethodPoolConfigKeys() {
        return methodPoolConfigKeys;
    }

    public static Map<String, String> getSpringPoolBeanCoreSizeKeys() {
        return springPoolBeanCoreSizeKeys;
    }

    public static Map<String, String> getSpringPoolBeanMaxSizeKeys() {
        return springPoolBeanMaxSizeKeys;
    }

    public static Map<String, String> getSpringPoolBeanQueueSizeKeys() {
        return springPoolBeanQueueSizeKeys;
    }

    public static void setSharedPoolQueueSizeKey(String sharedPoolQueueSizeKey) {
        RequestThreadPoolProcessor.sharedPoolQueueSizeKey = sharedPoolQueueSizeKey;
    }

    public static void setSharedPoolMaxSizeKey(String sharedPoolMaxSizeKey) {
        RequestThreadPoolProcessor.sharedPoolMaxSizeKey = sharedPoolMaxSizeKey;
    }

    public static void setSharedPoolCoreSizeKey(String sharedPoolCoreSizeKey) {
        RequestThreadPoolProcessor.sharedPoolCoreSizeKey = sharedPoolCoreSizeKey;
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith(KEY_PROVIDER_POOL_CONFIG_ENABLE)) {
                if ("true".equals(value)) {
                    try {
                        init();
                    } catch (Throwable t) {
                        logger.warn("failed to refresh pool config, fallback to previous settings, please check...", t);
                    }
                } else if ("false".equals(value)) {
                    apiPoolConfigMapping = Maps.newConcurrentMap();
                    for (PoolBean poolBean : poolNameMapping.values()) {
                        poolBean.closeThreadPool();
                    }
                    poolNameMapping = Maps.newConcurrentMap();
                }
            } else if (key.endsWith(KEY_PROVIDER_POOL_CONFIG)) {
                if (configManager.getBooleanValue(KEY_PROVIDER_POOL_CONFIG_ENABLE, false)) {
                    try {
                        refreshPoolConfig(value);
                    } catch (Throwable t) {
                        logger.warn("failed to refresh pool config, fallback to previous settings, please check...", t);
                    }
                }
            } else if (key.endsWith(KEY_PROVIDER_POOL_API_CONFIG)) {
                if (configManager.getBooleanValue(KEY_PROVIDER_POOL_CONFIG_ENABLE, false)) {
                    try {
                        refreshApiPoolConfig(value);
                    } catch (Throwable t) {
                        logger.warn("failed to refresh api pool config, fallback to previous settings, please check...", t);
                    }
                }
            } else if (key.endsWith("pigeon.provider.pool.slow.enable")) {
                enableSlowPool = Boolean.valueOf(value);
            } else if (key.endsWith("pigeon.timeout.cancelratio")) {
                cancelRatio = Float.valueOf(value);
            } else if (key.endsWith("pigeon.provider.pool.ratio.coresize")) {
                DEFAULT_POOL_RATIO_CORE = Float.valueOf(value);
            } else if (StringUtils.isNotBlank(sharedPoolCoreSizeKey) && key.endsWith(sharedPoolCoreSizeKey)) {
                int size = Integer.valueOf(value);
                if (size != sharedRequestProcessThreadPool.getExecutor().getCorePoolSize() && size >= 0) {
                    try {
                        ThreadPool oldPool = sharedRequestProcessThreadPool;
                        int queueSize = oldPool.getExecutor().getQueue().remainingCapacity()
                                + oldPool.getExecutor().getQueue().size();
                        try {
                            ThreadPool newPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method", size,
                                    oldPool.getExecutor().getMaximumPoolSize(), new LinkedBlockingQueue<Runnable>(
                                    queueSize));
                            sharedRequestProcessThreadPool = newPool;
                            oldPool.getExecutor().shutdown();
                            oldPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                            oldPool = null;
                        } catch (Throwable e) {
                            logger.warn("error when shuting down old shared pool", e);
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("changed shared pool, key:" + key + ", value:" + value);
                        }
                    } catch (RuntimeException e) {
                        logger.error("error while changing shared pool, key:" + key + ", value:" + value, e);
                    }
                }
            } else if (StringUtils.isNotBlank(sharedPoolMaxSizeKey) && key.endsWith(sharedPoolMaxSizeKey)) {
                int size = Integer.valueOf(value);
                if (size != sharedRequestProcessThreadPool.getExecutor().getMaximumPoolSize() && size >= 0) {
                    try {
                        ThreadPool oldPool = sharedRequestProcessThreadPool;
                        int queueSize = oldPool.getExecutor().getQueue().remainingCapacity()
                                + oldPool.getExecutor().getQueue().size();
                        try {
                            ThreadPool newPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method",
                                    oldPool.getExecutor().getCorePoolSize(), size, new LinkedBlockingQueue<Runnable>(
                                    queueSize));
                            sharedRequestProcessThreadPool = newPool;
                            oldPool.getExecutor().shutdown();
                            oldPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                            oldPool = null;
                        } catch (Throwable e) {
                            logger.warn("error when shuting down old shared pool", e);
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("changed shared pool, key:" + key + ", value:" + value);
                        }
                    } catch (RuntimeException e) {
                        logger.error("error while changing shared pool, key:" + key + ", value:" + value, e);
                    }
                }
            } else if (StringUtils.isNotBlank(sharedPoolQueueSizeKey) && key.endsWith(sharedPoolQueueSizeKey)) {
                int size = Integer.valueOf(value);
                ThreadPool oldPool = sharedRequestProcessThreadPool;
                int queueSize = oldPool.getExecutor().getQueue().remainingCapacity()
                        + oldPool.getExecutor().getQueue().size();
                if (size != queueSize && size >= 0) {
                    try {
                        try {
                            ThreadPool newPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method",
                                    oldPool.getExecutor().getCorePoolSize(),
                                    oldPool.getExecutor().getMaximumPoolSize(), new LinkedBlockingQueue<Runnable>(size));
                            sharedRequestProcessThreadPool = newPool;
                            oldPool.getExecutor().shutdown();
                            oldPool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                            oldPool = null;
                        } catch (Throwable e) {
                            logger.warn("error when shuting down old shared pool", e);
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("changed shared pool, key:" + key + ", value:" + value);
                        }
                    } catch (RuntimeException e) {
                        logger.error("error while changing shared pool, key:" + key + ", value:" + value, e);
                    }
                }
            } else {
                for (Map.Entry<String, String> entry : springPoolBeanCoreSizeKeys.entrySet()) {
                    String poolBeanName = entry.getKey();
                    String poolBeanCoreSizeKey = entry.getValue();
                    if (key.endsWith(poolBeanCoreSizeKey)) {
                        try {
                            Integer coreSize = Integer.parseInt(value);
                            PoolBean poolBean = springPoolNameMapping.get(poolBeanName);
                            if (poolBean != null) {
                                if (coreSize < 0 || coreSize > poolBean.getMaxPoolSize()) {
                                    throw new IllegalArgumentException("core size is illegal, please check: " + coreSize);
                                }
                                poolBean.setCorePoolSize(coreSize);
                            }
                        } catch (RuntimeException e) {
                            logger.error("error while changing spring poolBean, key:" + key + ", value:" + value, e);
                        }
                    }
                }

                for (Map.Entry<String, String> entry : springPoolBeanMaxSizeKeys.entrySet()) {
                    String poolBeanName = entry.getKey();
                    String poolBeanMaxSizeKey = entry.getValue();
                    if (key.endsWith(poolBeanMaxSizeKey)) {
                        try {
                            Integer maxSize = Integer.parseInt(value);
                            PoolBean poolBean = springPoolNameMapping.get(poolBeanName);
                            if (poolBean != null) {
                                if (maxSize < poolBean.getCorePoolSize() || maxSize <= 0) {
                                    throw new IllegalArgumentException("max size is illegal, please check: " + maxSize);
                                }
                                poolBean.setMaxPoolSize(maxSize);
                            }
                        } catch (RuntimeException e) {
                            logger.error("error while changing spring poolBean, key:" + key + ", value:" + value, e);
                        }
                    }
                }

                for (Map.Entry<String, String> entry : springPoolBeanQueueSizeKeys.entrySet()) {
                    String poolBeanName = entry.getKey();
                    String poolBeanQueueSizeKey = entry.getValue();
                    if (key.endsWith(poolBeanQueueSizeKey)) {
                        try {
                            Integer queueSize = Integer.parseInt(value);
                            PoolBean poolBean = springPoolNameMapping.get(poolBeanName);
                            if (poolBean != null) {
                                if (queueSize < 0) {
                                    throw new IllegalArgumentException("queue size is illegal, please check: " + queueSize);
                                }
                                poolBean.setWorkQueueSize(queueSize);
                            }
                        } catch (RuntimeException e) {
                            logger.error("error while changing spring poolBean, key:" + key + ", value:" + value, e);
                        }
                    }
                }

                for (String k : methodPoolConfigKeys.keySet()) {
                    String v = methodPoolConfigKeys.get(k);
                    if (key.endsWith(v)) {
                        try {
                            String serviceKey = k;
                            if (StringUtils.isNotBlank(serviceKey)) {
                                ThreadPool pool = null;
                                if (!CollectionUtils.isEmpty(methodThreadPools)) {
                                    pool = methodThreadPools.get(serviceKey);
                                    int actives = Integer.valueOf(value);
                                    if (pool != null && actives != pool.getExecutor().getMaximumPoolSize()
                                            && actives >= 0) {
                                        int coreSize = (int) (actives / DEFAULT_POOL_RATIO_CORE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_CORE)
                                                : actives;
                                        int queueSize = actives;
                                        int maxSize = actives;
                                        try {
                                            ThreadPool newPool = new DefaultThreadPool(
                                                    "Pigeon-Server-Request-Processor-method", coreSize, maxSize,
                                                    new LinkedBlockingQueue<Runnable>(queueSize));
                                            methodThreadPools.put(serviceKey, newPool);
                                            pool.getExecutor().shutdown();
                                            pool.getExecutor().awaitTermination(5, TimeUnit.SECONDS);
                                            pool = null;
                                        } catch (Throwable e) {
                                            logger.warn("error when shuting down old method pool", e);
                                        }
                                        if (logger.isInfoEnabled()) {
                                            logger.info("changed method pool, key:" + serviceKey + ", value:" + actives);
                                        }
                                    }
                                }
                            }
                        } catch (RuntimeException e) {
                            logger.error("error while changing method pool, key:" + key + ", value:" + value, e);
                        }
                    }
                }
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {
        }

        @Override
        public void onKeyRemoved(String key) {
        }

    }
}
