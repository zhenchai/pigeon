package com.dianping.pigeon.registry.listener;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;

/**
 * Created by chenchongze on 17/5/4.
 */
public class RegistryNotifyListenerLoader {

    private static RegistryNotifyListener registryNotifyListener = ExtensionLoader.getExtension(RegistryNotifyListener.class);
    private static final Logger logger = LoggerLoader.getLogger(RegistryNotifyListener.class);

    static {
        if (registryNotifyListener == null) {
            registryNotifyListener = new DefaultRegistryNotifyListener();
        }
        logger.info("registryNotifyListener: " + registryNotifyListener.getClass().getName());
    }

    public static RegistryNotifyListener getRegistryNotifyListener() {
        return registryNotifyListener;
    }
}
