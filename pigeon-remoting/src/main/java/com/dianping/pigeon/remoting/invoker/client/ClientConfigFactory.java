package com.dianping.pigeon.remoting.invoker.client;

import com.dianping.pigeon.config.ConfigManager;

/**
 * @author qi.yin
 *         2016/11/02  下午2:03.
 */
public class ClientConfigFactory {

    public static volatile ClientConfig clientConfig;

    public static ClientConfig createClientConfig(ConfigManager configManager) {
        if (clientConfig == null) {

            synchronized (ClientConfigFactory.class) {
                if (clientConfig == null) {
                    clientConfig = new ClientConfig(configManager);
                }
            }
        }

        return clientConfig;

    }
}
