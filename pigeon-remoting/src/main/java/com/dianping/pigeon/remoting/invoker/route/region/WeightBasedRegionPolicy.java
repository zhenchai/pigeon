package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.exception.RouteException;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * Created by chenchongze on 16/4/15.
 */
public class WeightBasedRegionPolicy implements RegionPolicy {

    public final static WeightBasedRegionPolicy INSTANCE = new WeightBasedRegionPolicy();

    private WeightBasedRegionPolicy() {
    }

    public static final String NAME = "weightBased";

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;
    private final RequestQualityManager requestQualityManager = RequestQualityManager.INSTANCE;
    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private final Random random = new Random();

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvocationRequest request) {
        return getRegionActiveClients(clientList, request);
    }

    private List<Client> getRegionActiveClients(List<Client> clientList, InvocationRequest request) {
        // 分region存储clients
        Map<Region, List<Client>> regionClientsMap = new HashMap<Region, List<Client>>();
        List<Region> regionArrays = Lists.newArrayList(regionPolicyManager.getRegionArray());

        for (Region region : regionArrays) {
            regionClientsMap.put(region, new ArrayList<Client>());
        }

        for (Client client : clientList) {
            List<Client> regionClients = regionClientsMap.get(client.getRegion());
            if (regionClients != null) {
                regionClients.add(client);
            }
        }

        // 初始化region中存在可用client的权重和
        Integer weightSum = 0;
        Set<Region> regionSet = new HashSet<Region>();

        for (Region region : regionClientsMap.keySet()) {
            if (regionClientsMap.get(region).size() > 0) {
                weightSum += region.getWeight();
                regionSet.add(region);
            }
        }

        if (weightSum <= 0) {
            throw new RouteException("Error: weightSum=" + weightSum.toString());
        }

        // 权重随机算法
        Integer n = random.nextInt(weightSum); // n in [0, weightSum)
        Integer m = 0;

        for (Region region : regionSet) {
            int weight = region.getWeight();
            List<Client> regionClientList = regionClientsMap.get(region);

            if (m <= n && n < m + weight) {

                return regionClientList;

            }

            m += weight;
        }

        return clientList;
    }

}
