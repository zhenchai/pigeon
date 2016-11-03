package com.dianping.pigeon.remoting.provider.publish;

import com.dianping.pigeon.remoting.provider.config.ProviderConfig;

/**
 * Created by chenchongze on 16/11/3.
 */
public interface PublishPolicy {

    void init();

    void doAddService(ProviderConfig providerConfig);
}
