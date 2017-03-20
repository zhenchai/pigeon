package com.dianping.pigeon.log;

public class SimpleLogger implements Logger {

    private String loggerName;

    public SimpleLogger() {
        this.loggerName = this.getClass().getName();
    }

    public SimpleLogger(String loggerName) {
        this.loggerName = loggerName;
    };

    @Override
    public void debug(Object message) {
        System.out.println(message);
    }

    @Override
    public void debug(Object message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void debug(String message) {
        System.out.println(message);
    }

    @Override
    public void debug(String message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void error(Object message) {
        System.err.println(message);
    }

    @Override
    public void error(Object message, Throwable t) {
        System.err.println(message);
        t.printStackTrace(System.err);
    }

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void info(String message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isFatalEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void trace(Object message) {
        System.out.println(message);
    }

    @Override
    public void trace(Object message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void trace(String message) {
        System.out.println(message);
    }

    @Override
    public void trace(String message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void warn(Object message) {
        System.out.println(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void warn(String message) {
        System.out.println(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }

    @Override
    public void error(String message, Throwable t) {
        System.err.println(message);
        t.printStackTrace(System.err);
    }

    @Override
    public void fatal(Object message) {
        System.err.println(message);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        System.err.println(message);
        t.printStackTrace(System.err);
    }

    @Override
    public void fatal(String message) {
        System.err.println(message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        System.err.println(message);
        t.printStackTrace(System.err);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void info(Object message) {
        System.out.println(message);
    }

    @Override
    public void info(Object message, Throwable t) {
        System.out.println(message);
        t.printStackTrace(System.out);
    }

}
