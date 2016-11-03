package com.dianping.pigeon.remoting.provider.publish;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import org.apache.commons.lang.StringUtils;

/**
 * Created by chenchongze on 16/11/3.
 */
public class AbstractPublishPolicy implements PublishPolicy {

    private static Logger logger = LoggerLoader.getLogger(AbstractPublishPolicy.class);

    @Override
    public void init() {

    }

    @Override
    public void doAddService(ProviderConfig providerConfig) {
        try {
            checkServiceName(providerConfig);
            ServicePublisher.addService(providerConfig);
            ServerConfig serverConfig = ProviderBootStrap.startup(providerConfig);
            providerConfig.setServerConfig(serverConfig);
            ServicePublisher.publishService(providerConfig, false);
        } catch (Throwable t) {
            throw new RpcException("error while adding service:" + providerConfig, t);
        }
    }

    private void checkServiceName(ProviderConfig providerConfig) {
        if (StringUtils.isBlank(providerConfig.getUrl())) {
            providerConfig.setUrl(ServiceFactory.getServiceUrl(providerConfig));
        } else if (providerConfig.getUrl().contains("#")) {
            throw new IllegalArgumentException("service name cannot contains '#' symbol: " + providerConfig.getUrl());
        } else if (providerConfig.isSupported() && !ServiceFactory.getServiceUrl(providerConfig).equals(providerConfig.getUrl())) {
            logger.warn("customized [serviceName] cannot provide service to OCTO invoker "
                    + "unless set the [serviceName] to canonical name of the interface class "
                    + "or just keep [serviceName] config to blank. more help refer to: "
                    + ConfigManagerLoader.getConfigManager().getStringValue("pigeon.help.provider.octo.url"
                    , "http://wiki.sankuai.com/pages/viewpage.action?pageId=606809899"));
        }
    }
}
