package com.dianping.pigeon.remoting.common.codec;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;

/**
 * @author qi.yin
 *         2016/12/09  下午5:14.
 */
public class CodecConfigFactory {

    public static volatile CodecConfig codecConfig;

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    public static CodecConfig createClientConfig() {
        if (codecConfig == null) {

            synchronized (CodecConfigFactory.class) {
                if (codecConfig == null) {
                    codecConfig = new CodecConfig(configManager);
                }
            }
        }

        return codecConfig;

    }
}
