/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.domain.Disposable;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.route.balance.RandomLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.WeightedAutoawareLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRouteManager implements RouteManager, Disposable {

    private static final Logger logger = LoggerLoader.getLogger(DefaultRouteManager.class);

    public final static DefaultRouteManager INSTANCE = new DefaultRouteManager();

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;

    private final RequestQualityManager requestQualityManager = RequestQualityManager.INSTANCE;

    private static final ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

    private ServiceProviderChangeListener providerChangeListener = new InnerServiceProviderChangeListener();

    private static List<String> preferAddresses = null;

    private static boolean enablePreferAddresses = ConfigManagerLoader.getConfigManager().getBooleanValue(
            "pigeon.route.preferaddresses.enable", false);

    private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();
    private static final String KEY_LOADBALANCE_DYNAMICTYPE = "pigeon.loadbalance.dynamictype";

    // s1#m1-->lbName or s1-->lbName
    private static volatile Map<String, String> dynamicLoadBalanceTypes = new HashMap<>();

    static {
        String dynamicLoadBalanceConfig = ConfigManagerLoader.getConfigManager().getStringValue(KEY_LOADBALANCE_DYNAMICTYPE);
        parseDynamicLoadBalanceConfig(dynamicLoadBalanceConfig);
        ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
    }

    private static void parseDynamicLoadBalanceConfig(String dynamicLoadBalanceConfig) {
        if (StringUtils.isNotBlank(dynamicLoadBalanceConfig)) {
            Map<String, String> map;
            try {
                map = (HashMap) jacksonSerializer.toObject(HashMap.class, dynamicLoadBalanceConfig);
                dynamicLoadBalanceTypes = map;
            } catch (Throwable t) {
                logger.warn("error while parsing dynamic loadbalance configuration:" + dynamicLoadBalanceConfig, t);
            }
        } else {
            dynamicLoadBalanceTypes = new HashMap<>();
        }
    }

    private DefaultRouteManager() {
        RegistryEventListener.addListener(providerChangeListener);
        if (enablePreferAddresses) {
            preferAddresses = new ArrayList<String>();
            String preferAddressesConfig = ConfigManagerLoader.getConfigManager().getStringValue(
                    "pigeon.route.preferaddresses", "");
            String[] preferAddressesArray = preferAddressesConfig.split(",");
            for (String addr : preferAddressesArray) {
                if (StringUtils.isNotBlank(addr)) {
                    preferAddresses.add(addr.trim());
                }
            }
        }
    }

    public Client route(List<Client> clientList, InvokerConfig<?> invokerConfig, InvocationRequest request) {
        if (logger.isDebugEnabled()) {
            for (Client client : clientList) {
                if (client != null) {
                    logger.debug("available service provider：\t" + client.getAddress());
                }
            }
        }
        List<Client> availableClients = getAvailableClients(clientList, invokerConfig, request);
        Client selectedClient = select(availableClients, invokerConfig, request);

        while (!selectedClient.isActive()) {
            logger.info("[route] remove client:" + selectedClient);
            availableClients.remove(selectedClient);
            if (availableClients.isEmpty()) {
                break;
            }
            selectedClient = select(availableClients, invokerConfig, request);
        }

        if (!selectedClient.isActive()) {
            throw new ServiceUnavailableException("no available server exists for service[" + invokerConfig + "], env:"
                    + ConfigManagerLoader.getConfigManager().getEnv());
        }
        return selectedClient;
    }

    /**
     * 按照权重、分组、region规则过滤客户端选择 加入对oneway调用模式的优化判断
     *
     * @param clientList
     * @param invokerConfig
     * @param request
     * @return
     */
    public List<Client> getAvailableClients(List<Client> clientList, InvokerConfig<?> invokerConfig,
                                            InvocationRequest request) {

        if (regionPolicyManager.isEnableRegionPolicy()) {

            clientList = regionPolicyManager.getPreferRegionClients(clientList, invokerConfig, request);

        }

        List<Client> filteredClients = new ArrayList<Client>(clientList.size());
        for (Client client : clientList) {
            if (client != null) {
                String address = client.getAddress();
                int weight = RegistryManager.getInstance().getServiceWeightFromCache(address);
                if (client.isActive() && weight > 0) {
                    filteredClients.add(client);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("provider status:" + client.isActive() + "," + weight);
                }
            }
        }
        if (filteredClients.isEmpty()) {
            throw new ServiceUnavailableException("no available server exists for service[" + invokerConfig.getUrl()
                    + "] and group[" + RegistryManager.getInstance().getGroup(invokerConfig.getUrl()) + "].");
        }
        return filteredClients;
    }

    private void checkClientNotNull(Client client, InvokerConfig<?> invokerConfig) {
        if (client == null) {
            throw new ServiceUnavailableException("no available server exists for service[" + invokerConfig + "], env:"
                    + ConfigManagerLoader.getConfigManager().getEnv());
        }
    }

    private Client select(List<Client> availableClients, InvokerConfig<?> invokerConfig, InvocationRequest request) {
        LoadBalance loadBalance = null;
        if (loadBalance == null) {
            loadBalance = LoadBalanceManager.getLoadBalance(invokerConfig, request.getCallType());
        }
        if (loadBalance == null) {
            loadBalance = WeightedAutoawareLoadBalance.instance;
            if (request.getCallType() == Constants.CALLTYPE_NOREPLY) {
                loadBalance = RandomLoadBalance.instance;
            }
        }

        // 判断是否有动态配置的loadbalance
        LoadBalance dynamicLoadBalance = getDynamicLoadBalance(request);
        if (dynamicLoadBalance != null) {
            loadBalance = dynamicLoadBalance;
        }

        List<Client> preferClients = null;
        if (enablePreferAddresses) {
            if (availableClients != null && availableClients.size() > 1 && !CollectionUtils.isEmpty(preferAddresses)) {
                preferClients = new ArrayList<Client>();
                for (String addr : preferAddresses) {
                    for (Client client : availableClients) {
                        if (client.getHost().startsWith(addr)) {
                            preferClients.add(client);
                        }
                    }
                    if (preferClients.size() > 0) {
                        break;
                    }
                }
            }
        }
        if (preferClients == null || preferClients.size() == 0) {
            preferClients = availableClients;
        }
        Client selectedClient = loadBalance.select(preferClients, invokerConfig, request);
        checkClientNotNull(selectedClient, invokerConfig);

        return selectedClient;
    }

    private LoadBalance getDynamicLoadBalance(InvocationRequest request) {
        String loadBalanceName = dynamicLoadBalanceTypes.get(request.getServiceName() + "#" + request.getMethodName());
        if (StringUtils.isBlank(loadBalanceName)) { // fallback to service config
            loadBalanceName = dynamicLoadBalanceTypes.get(request.getServiceName());
        }

        if (StringUtils.isNotBlank(loadBalanceName)) {
            return LoadBalanceManager.getLoadBalance(loadBalanceName);
        }

        return null;
    }

    @Override
    public void destroy() throws Exception {
        RegistryEventListener.removeListener(providerChangeListener);
    }

    private static class InnerConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onKeyUpdated(String key, String value) {
            try {
                if (key.endsWith(KEY_LOADBALANCE_DYNAMICTYPE)) {
                    parseDynamicLoadBalanceConfig(value);
                }
            } catch (Throwable t) {
                logger.warn("invalid value for key " + key, t);
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }

    class InnerServiceProviderChangeListener implements ServiceProviderChangeListener {
        @Override
        public void hostWeightChanged(ServiceProviderChangeEvent event) {
            RegistryManager.getInstance().setServiceWeight(event.getConnect(), event.getWeight());
        }

        @Override
        public void providerAdded(ServiceProviderChangeEvent event) {
        }

        @Override
        public void providerRemoved(ServiceProviderChangeEvent event) {
        }
    }

}
