package com.dianping.pigeon.log;

import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;

import com.dianping.pigeon.util.AppUtils;

public class LoggerLoader {

    private static LoggerContext context = null;
    private static final String LOG_ROOT_KEY = "pigeon.log.dir";
    private static final String LOG_ROOT_DEFAULT = "/data/applogs/pigeon";

    public static String LOG_ROOT;

    static {
        init();
    }

    private LoggerLoader() {
    }

    public static synchronized void init() {
        if (context == null) {
            if (StringUtils.isBlank(System.getProperty(LOG_ROOT_KEY))) {
                System.setProperty(LOG_ROOT_KEY, LOG_ROOT_DEFAULT);
            }
            LOG_ROOT = System.getProperty(LOG_ROOT_KEY);
            String appName = AppUtils.getAppName();
            System.setProperty("app.name", appName);

            URL url = LoggerLoader.class.getResource("log4j2-pigeon.xml");
            LoggerContext ctx;
            if (url == null) {
                ctx = LogManager.getContext(false);
            } else {
                try {
                    ctx = new org.apache.logging.log4j.core.LoggerContext("Pigeon", null, url.toURI());
                    ((org.apache.logging.log4j.core.LoggerContext) ctx).start();
                } catch (Throwable t) {
                    String errMsg = "[" + LoggerLoader.class.getName() + "] Failed to initialize log4j2..."
                            + "Please check the write permission of pigeon log dir: (" + LOG_ROOT + ").";
                    System.err.println(errMsg);
                    ctx = LogManager.getContext(false);
                }
            }
            context = ctx;
        }
    }

    public static Logger getLogger(Class<?> className) {
        return getLogger(className.getName());
    }

    public static Logger getLogger(String name) {
        if (context == null) {
            init();
        }
        return new SimpleLogger(context.getLogger(name));
    }

    public static LoggerContext getLoggerContext() {
        return context;
    }
}
