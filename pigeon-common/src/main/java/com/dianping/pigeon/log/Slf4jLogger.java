package com.dianping.pigeon.log;

import com.dianping.pigeon.util.Constants;
import org.slf4j.spi.LocationAwareLogger;

public class Slf4jLogger implements Logger {

    private org.slf4j.Logger logger;

    public Slf4jLogger() {
        this(Slf4jLogger.class.getName());
    }

    private boolean isLocationAware = false;

    private static final String ADAPTER_FQCN = Slf4jLogger.class.getName();

    public Slf4jLogger(String loggerName) {
        this.logger = org.slf4j.LoggerFactory.getLogger(loggerName);
        if (!Constants.ACCESS_LOG_NAME.equals(loggerName) && this.logger instanceof LocationAwareLogger) {
            try {
                ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                        LocationAwareLogger.DEBUG_INT, "init slf4j logger", null, null);
                isLocationAware = true;
            } catch(Throwable t) {
                isLocationAware = false;
            }
        }
    }

    @Override
    public void debug(Object message) {
        debug(message.toString(), null);
    }

    @Override
    public void debug(Object message, Throwable t) {
        debug(message.toString(), t);
    }

    @Override
    public void debug(String message) {
        debug(message, null);
    }

    @Override
    public void debug(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.DEBUG_INT, message, null, t);
        } else {
            logger.debug(message, t);
        }
    }

    @Override
    public void error(Object message) {
        error(message.toString(), null);
    }

    @Override
    public void error(Object message, Throwable t) {
        error(message.toString(), t);
    }

    @Override
    public void error(String message) {
        error(message, null);
    }

    @Override
    public void error(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.ERROR_INT, message, null, t);
        } else {
            logger.error(message, t);
        }
    }

    @Override
    public void info(String message) {
        info(message, null);
    }

    @Override
    public void info(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.INFO_INT, message, null, t);
        } else {
            logger.info(message, t);
        }
    }

    @Override
    public void info(Object message) {
        info(message.toString(), null);
    }

    @Override
    public void info(Object message, Throwable t) {
        info(message.toString(), t);
    }

    @Override
    public void trace(Object message) {
        trace(message.toString(), null);
    }

    @Override
    public void trace(Object message, Throwable t) {
        trace(message.toString(), t);
    }

    @Override
    public void trace(String message) {
        trace(message, null);
    }

    @Override
    public void trace(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.TRACE_INT, message, null, t);
        } else {
            logger.trace(message, t);
        }
    }

    @Override
    public void warn(Object message) {
        warn(message.toString(), null);
    }

    @Override
    public void warn(Object message, Throwable t) {
        warn(message.toString(), t);
    }

    @Override
    public void warn(String message) {
        warn(message, null);
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.WARN_INT, message, null, t);
        } else {
            logger.warn(message, t);
        }
    }

    @Override
    public void fatal(Object message) {
        error(message.toString(), null);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        error(message.toString(), t);
    }

    @Override
    public void fatal(String message) {
        error(message, null);
    }

    @Override
    public void fatal(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.ERROR_INT, message, null, t);
        } else {
            logger.error(message, t);
        }
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }
}
