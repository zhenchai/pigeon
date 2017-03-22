package com.dianping.pigeon.log;

public class NullLogger implements Logger {

    public NullLogger() {
    }

    public NullLogger(String loggerName) {}

    @Override
    public void debug(Object message) {

    }

    @Override
    public void debug(Object message, Throwable t) {

    }

    @Override
    public void debug(String message) {

    }

    @Override
    public void debug(String message, Throwable t) {

    }

    @Override
    public void error(Object message) {

    }

    @Override
    public void error(Object message, Throwable t) {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String message, Throwable t) {

    }

    @Override
    public void fatal(Object message) {

    }

    @Override
    public void fatal(Object message, Throwable t) {

    }

    @Override
    public void fatal(String message) {

    }

    @Override
    public void fatal(String message, Throwable t) {

    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void info(Object message) {

    }

    @Override
    public void info(Object message, Throwable t) {

    }

    @Override
    public void info(String message) {

    }

    @Override
    public void info(String message, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public boolean isFatalEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void trace(Object message) {

    }

    @Override
    public void trace(Object message, Throwable t) {

    }

    @Override
    public void trace(String message) {

    }

    @Override
    public void trace(String message, Throwable t) {

    }

    @Override
    public void warn(Object message) {

    }

    @Override
    public void warn(Object message, Throwable t) {

    }

    @Override
    public void warn(String message) {

    }

    @Override
    public void warn(String message, Throwable t) {

    }
}
