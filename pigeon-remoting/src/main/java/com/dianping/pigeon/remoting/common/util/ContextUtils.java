package com.dianping.pigeon.remoting.common.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ContextUtils {

    private static ThreadLocal<Map> localContext = new ThreadLocal<Map>();

    private static ThreadLocal<Map<String, Serializable>> globalContext = new ThreadLocal<Map<String, Serializable>>();

    private static ThreadLocal<Map<String, Serializable>> requestContext = new ThreadLocal<Map<String, Serializable>>();

    private static ThreadLocal<Map<String, Serializable>> responseContext = new ThreadLocal<Map<String, Serializable>>();

    public static void putLocalContext(Object key, Object value) {
        Map<Object, Object> context = localContext.get();
        if (context == null) {
            context = new HashMap<Object, Object>();
            localContext.set(context);
        }
        context.put(key, value);
    }

    public static Map getLocalContext() {
        return localContext.get();
    }

    public static Object getLocalContext(Object key) {
        Map context = localContext.get();
        if (context == null) {
            return null;
        }
        return context.get(key);
    }

    public static void clearLocalContext() {
        Map context = localContext.get();
        if (context != null) {
            context.clear();
        }
        localContext.remove();
    }

    public static void putGlobalContext(String key, Serializable value) {
        Map<String, Serializable> context = globalContext.get();
        if (context == null) {
            context = new HashMap<String, Serializable>();
            globalContext.set(context);
        }
        context.put(key, value);
    }

    public static void setGlobalContext(Map<String, Serializable> context) {
        globalContext.set(context);
    }

    public static Map<String, Serializable> getGlobalContext() {
        return globalContext.get();
    }

    public static Serializable getGlobalContext(String key) {
        Map<String, Serializable> context = globalContext.get();
        if (context == null) {
            return null;
        }
        return context.get(key);
    }

    public static void clearGlobalContext() {
        Map<String, Serializable> context = globalContext.get();
        if (context != null) {
            context.clear();
        }
        globalContext.remove();
    }

    public static void initRequestContext() {
        Map<String, Serializable> context = requestContext.get();
        if (context == null) {
            context = new HashMap<String, Serializable>();
            requestContext.set(context);
        }
    }

    public static void putRequestContext(String key, Serializable value) {
        Map<String, Serializable> context = requestContext.get();
        if (context == null) {
            context = new HashMap<String, Serializable>();
            requestContext.set(context);
        }
        context.put(key, value);
    }

    public static Map<String, Serializable> getRequestContext() {
        return requestContext.get();
    }

    public static Serializable getRequestContext(String key) {
        Map<String, Serializable> context = requestContext.get();
        if (context == null) {
            return null;
        }
        return context.get(key);
    }

    public static void clearRequestContext() {
        Map<String, Serializable> context = requestContext.get();
        if (context != null) {
            context.clear();
        }
        requestContext.remove();
    }

    public static void putResponseContext(String key, Serializable value) {
        Map<String, Serializable> context = responseContext.get();
        if (context == null) {
            context = new HashMap<String, Serializable>();
            responseContext.set(context);
        }
        context.put(key, value);
    }

    public static Map<String, Serializable> getResponseContext() {
        return responseContext.get();
    }

    public static Serializable getResponseContext(String key) {
        Map<String, Serializable> context = responseContext.get();
        if (context == null) {
            return null;
        }
        return context.get(key);
    }

    public static void setResponseContext(Map<String, Serializable> context) {
        responseContext.set(context);
    }

    public static void clearResponseContext() {
        Map<String, Serializable> context = responseContext.get();
        if (context != null) {
            context.clear();
        }
        responseContext.remove();
    }

    public static Long getRemainedTimeMillis() {
        Integer timeout = (Integer) getLocalContext(Constants.CONTEXT_KEY_TIMEOUT_MILLIS);
        Long createTime = (Long) getLocalContext(Constants.CONTEXT_KEY_CREATE_TIME_MILLIS);
        if (createTime == null || timeout == null) {
            return null;
        } else {
            return timeout - (System.currentTimeMillis() - createTime);
        }
    }

    public static void convertContext(Map<String, Serializable> srcCtx, Map<String, String> dstCtx) {
        if (srcCtx != null) {
            for (Map.Entry<String, Serializable> entry : srcCtx.entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getValue() instanceof String) {
                        dstCtx.put(entry.getKey(), (String) entry.getValue());
                    } else {
                        throw new IllegalArgumentException("only support string type.");
                    }
                }

            }
        }
    }

    public static Map<String, Serializable> convertContext(Map<String, String> srcCtx) {
        Map<String, Serializable> dstCtx = new HashMap<String, Serializable>();

        if (srcCtx != null) {
            for (Map.Entry<String, String> entry : srcCtx.entrySet()) {
                dstCtx.put(entry.getKey(), entry.getValue());
            }
        }

        return dstCtx;
    }
}
