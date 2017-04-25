package com.dianping.pigeon.log;

/**
 * Created by chenchongze on 17/4/25.
 */
public interface LoggerInitializer {


    void init();

    Logger getLogger(String loggerName);
}
