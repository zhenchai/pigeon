package com.dianping.pigeon.remoting.provider.publish;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;

/**
 * Created by chenchongze on 16/11/3.
 */
public class PublishPolicyLoader {
    private static PublishPolicy publishPolicy = ExtensionLoader.getExtension(PublishPolicy.class);
    private static final Logger logger = LoggerLoader.getLogger(PublishPolicyLoader.class);

    static {
        if (publishPolicy == null) {
            publishPolicy = new DefaultPublishPolicy();
        }
        logger.info("publishPolicy: " + publishPolicy);
        publishPolicy.init();
    }

    public static PublishPolicy getPublishPolicy() {
        return publishPolicy;
    }
}
