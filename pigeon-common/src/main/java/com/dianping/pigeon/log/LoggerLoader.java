package com.dianping.pigeon.log;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;

public class LoggerLoader {

    private static Constructor logConstructor;

    private static final String LOG_ROOT_KEY = "pigeon.log.dir";
    private static final String LOG_ROOT_DEFAULT = "/data/applogs/pigeon";

    public static String LOG_ROOT;

    static {
        if (StringUtils.isBlank(System.getProperty(LOG_ROOT_KEY))) {
            System.setProperty(LOG_ROOT_KEY, LOG_ROOT_DEFAULT);
        }
        LOG_ROOT = System.getProperty(LOG_ROOT_KEY);

        String logType = System.getProperty("pigeon.logType");
        if (logType != null) {
            if (logType.equalsIgnoreCase("log4j2")) {
                tryImplementation("org.apache.logging.log4j.Logger", "com.dianping.pigeon.log.Log4j2Logger");
            } else if (logType.equalsIgnoreCase("slf4j")) {
                tryImplementation("org.slf4j.Logger", "com.dianping.pigeon.log.Slf4jLogger");
            } else if(logType.equalsIgnoreCase("simple")) {
                tryImplementation(null, "com.dianping.pigeon.log.SimpleLogger");
            } else if(logType.equalsIgnoreCase("null")) {
                tryImplementation(null, "com.dianping.pigeon.log.NullLogger");
            }
        }

        // slf4j > log4j2 > simple > null
        tryImplementation("org.slf4j.Logger", "com.dianping.pigeon.log.Slf4jLogger");
        tryImplementation("org.apache.logging.log4j.Logger", "com.dianping.pigeon.log.Log4j2Logger");

        if (logConstructor == null) {
            try {
                logConstructor = SimpleLogger.class.getConstructor(String.class);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void tryImplementation(String testClassName, String implClassName) {
        if (logConstructor != null) {
            return;
        }

        try {
            if(testClassName != null) {
                Resources.classForName(testClassName);
            }
            Class implClass = Resources.classForName(implClassName);
            logConstructor = implClass.getConstructor(new Class[] {
                    String.class
            });

            Class<?> declareClass = logConstructor.getDeclaringClass();
            if (!Logger.class.isAssignableFrom(declareClass)) {
                logConstructor = null;
            }

            try {
                if (null != logConstructor) {
                    logConstructor.newInstance(LoggerLoader.class.getName());
                }
            } catch (Throwable t) {
                t.printStackTrace();
                logConstructor = null;
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String loggerName) {
        try {
            return (Logger) logConstructor.newInstance(loggerName);
        } catch (Throwable t) {
            throw new RuntimeException("failed to create logger for " + loggerName + ", cause: " + t, t);
        }
    }
}
