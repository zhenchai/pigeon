package com.dianping.pigeon.remoting.provider.config.spring;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Created by chenchongze on 16/11/28.
 */
public class PoolInitializeListener implements ApplicationListener {



    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // TODO: 16/11/28 得到 poolName --> poolBean 缓存
    }
}
