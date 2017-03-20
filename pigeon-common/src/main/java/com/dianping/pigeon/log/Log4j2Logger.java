package com.dianping.pigeon.log;

import com.dianping.pigeon.util.AppUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;

import java.net.URL;

public class Log4j2Logger implements Logger {

    private org.apache.logging.log4j.Logger LOG;

    private static volatile boolean isDebugEnabled = false;

    private static LoggerContext context = null;

    public Log4j2Logger() {
        this(Log4j2Logger.class.getName());
    }

    public static synchronized void init() {
        if (context == null) {
            String appName = AppUtils.getAppName();
            System.setProperty("app.name", appName);

            URL url = Log4j2Logger.class.getResource("log4j2-pigeon.xml");
            LoggerContext ctx;
            if (url == null) {
                ctx = LogManager.getContext(false);
            } else {
                try {
                    ctx = new org.apache.logging.log4j.core.LoggerContext("Pigeon", null, url.toURI());
                    ((org.apache.logging.log4j.core.LoggerContext) ctx).start();
                } catch (Throwable t) {
                    String errMsg = "[" + LoggerLoader.class.getName() + "] Failed to initialize log4j2..."
                            + "Please check the write permission of pigeon log dir: (" + LoggerLoader.LOG_ROOT + ").";
                    System.err.println(errMsg);
                    ctx = LogManager.getContext(false);
                }
            }
            context = ctx;
        }
    }

    public Log4j2Logger(String loggerName) {
        if (context == null) {
            init();
        }
        this.LOG = context.getLogger(loggerName);
    }

    @Override
    public void debug(Object message) {
        if (this.isDebugEnabled()) {
            this.LOG.debug(message);
        }
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (this.isDebugEnabled()) {
            this.LOG.debug(message, t);
        }
    }

    @Override
    public void debug(String message) {
        if (this.isDebugEnabled()) {
            this.LOG.debug(message);
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        if (this.isDebugEnabled()) {
            this.LOG.debug(message, t);
        }
    }

    @Override
    public void error(Object message) {
        this.LOG.error(message);
    }

    @Override
    public void error(Object message, Throwable t) {
        this.LOG.error(message, t);
    }

    @Override
    public void error(String message) {
        this.LOG.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        this.LOG.error(message, t);
    }

    @Override
    public void fatal(Object message) {
        this.LOG.fatal(message);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        this.LOG.fatal(message, t);
    }

    @Override
    public void fatal(String message) {
        this.LOG.fatal(message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        this.LOG.fatal(message, t);
    }

    @Override
    public String getName() {
        return this.LOG.getName();
    }

    @Override
    public void info(Object message) {
        this.LOG.info(message);
    }

    @Override
    public void info(Object message, Throwable t) {
        this.LOG.info(message, t);
    }

    @Override
    public void info(String message) {
        this.LOG.info(message);
    }

    @Override
    public void info(String message, Throwable t) {
        this.LOG.info(message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public static void setDebugEnabled(boolean enabled) {
        isDebugEnabled = enabled;
    }

    @Override
    public boolean isErrorEnabled() {
        return this.LOG.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return this.LOG.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return this.LOG.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.LOG.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return this.LOG.isWarnEnabled();
    }

    @Override
    public void trace(Object message) {
        this.LOG.trace(message);
    }

    @Override
    public void trace(Object message, Throwable t) {
        this.LOG.trace(message, t);
    }

    @Override
    public void trace(String message) {
        this.LOG.trace(message);
    }

    @Override
    public void trace(String message, Throwable t) {
        this.LOG.trace(message, t);
    }

    @Override
    public void warn(Object message) {
        this.LOG.warn(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        this.LOG.warn(message, t);
    }

    @Override
    public void warn(String message) {
        this.LOG.warn(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        this.LOG.warn(message, t);
    }

}
