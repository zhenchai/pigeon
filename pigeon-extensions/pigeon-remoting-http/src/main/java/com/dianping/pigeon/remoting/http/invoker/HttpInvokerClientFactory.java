/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.invoker;

import java.util.Map;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.http.HttpUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientFactory;
import com.dianping.pigeon.remoting.invoker.client.ClientConfig;
import com.dianping.pigeon.remoting.invoker.client.ClientConfigFactory;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessorFactory;

public class HttpInvokerClientFactory implements ClientFactory {

    private final static ResponseProcessor responseProcessor =
            ResponseProcessorFactory.selectProcessor();

    private final static ClientConfig clientConfig = ClientConfigFactory.createClientConfig(ConfigManagerLoader.getConfigManager());

    @Override
    public Client createClient(ConnectInfo connectInfo) {
        return new HttpInvokerClient(clientConfig, connectInfo, responseProcessor);
    }

    @Override
    public boolean support(ConnectInfo connectInfo) {
        Map<String, Integer> serviceNames = connectInfo.getServiceNames();
        if (serviceNames != null && !serviceNames.isEmpty()) {
            String name = serviceNames.keySet().iterator().next();
            if (name.startsWith(HttpUtils.URL_PREFIX)) {
                return true;
            }
        }
        return false;
    }
}
