package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 17/5/15.
 */
public class ForceRegionPolicy implements RegionPolicy {

    public static final ForceRegionPolicy INSTANCE = new ForceRegionPolicy();
    public static final String NAME = "force";

    private final Logger logger = LoggerLoader.getLogger(this.getClass());
    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;
    private final RegistryManager registryManager = RegistryManager.getInstance();
    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private final Monitor monitor = MonitorLoader.getMonitor();

    private static final String KEY_REGION_THRESHOLD_RATIO = "pigeon.regions.switchratio";
    private volatile float regionSwitchRatio = configManager.getFloatValue(KEY_REGION_THRESHOLD_RATIO, 0.5f);
    private static final String KEY_REGION_FORCE_CONFIG = "pigeon.regions.force.config";
    private volatile String[] forceRegPrefer = new String[0];

    private ForceRegionPolicy() {
        String forceRegionConf = configManager.getStringValue(KEY_REGION_FORCE_CONFIG, "shanghai,beijing");
        initForceRegionConfig(forceRegionConf);
        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
    }

    private void initForceRegionConfig(String forceRegionConfig) {
        if (StringUtils.isNotBlank(forceRegionConfig)) {
            forceRegPrefer = forceRegionConfig.split(",");
        }
    }

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvocationRequest request) {
        List<Region> forceRegions = getForceRegionList();
        if (forceRegions != null) {
            return getRegionActiveClients(clientList, request, forceRegions);
        }

        return clientList;
    }

    private List<Region> getForceRegionList() {
        Map<String, Region> regionMap = regionPolicyManager.getRegionMap();
        if (forceRegPrefer.length != regionMap.size()) {
            logger.debug("Force region config size not match regions config, please check!");
            return null;
        }

        List<Region> _regionArr = new ArrayList<>();
        for (String reg : forceRegPrefer) {
            Region _region = regionMap.get(reg);
            if (_region == null) {
                logger.debug("Force region config not match regions config: " + reg);
                return null;
            }
            _regionArr.add(_region);
        }

        return _regionArr;
    }

    private List<Client> getRegionActiveClients(List<Client> clientList, InvocationRequest request, List<Region> regionArrays) {
        int sizeBefore = clientList.size();

        Map<Region, InnerRegionStat> regionStats = new HashMap<Region, InnerRegionStat>();

        for (Region region : regionArrays) { // 缓存每个region的统计信息
            regionStats.put(region, new InnerRegionStat());
        }

        for (Client client : clientList) { // 分发client的region统计信息
            try {
                InnerRegionStat regionStat = regionStats.get(client.getRegion());
                if (regionStat != null) {
                    regionStat.addTotal();
                    if (client.isActive() && registryManager.getServiceWeightFromCache(client.getAddress()) > 0) {
                        regionStat.addActive();
                        regionStat.addClient(client);
                    }
                }
            } catch (Throwable t) {
                logger.error(t);
            }
        }

        for (int i = 0; i < regionArrays.size(); ++i) {// 优先级大小按数组大小排列
            Region region = regionArrays.get(i);
            try {
                InnerRegionStat regionStat = regionStats.get(region);
                int total = regionStat.getTotal();
                int active = regionStat.getActive();
                List<Client> regionClientList = regionStat.getClientList();

                float least = regionSwitchRatio * total;

                if (total > 0 && active > 0 && active >= least) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("b: " + sizeBefore + ", a:" + regionClientList.size());
                    }
                    return regionClientList;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(request.getServiceName() + " skipped region " + region.getName()
                                + ", available clients less than " + least);
                    }
                    monitor.logEvent("PigeonCall.forceRegionUnavailable",
                            request.getServiceName() + "#" + region.getName(), "");
                }
            } catch (Throwable t) {
                logger.error(t);
            } finally {
                // maybe release work
            }
        }

        return clientList;
    }

    private class InnerRegionStat {
        private int active = 0;
        private int total = 0;
        private List<Client> clientList = new ArrayList<Client>();

        public List<Client> getClientList() {
            return clientList;
        }

        public void addClient(Client client) {
            clientList.add(client);
        }

        public int getActive() {
            return active;
        }

        public void addActive() {
            ++active;
        }

        public int getTotal() {
            return total;
        }

        public void addTotal() {
            ++total;
        }
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            try {
                if (key.endsWith(KEY_REGION_THRESHOLD_RATIO)) {
                    regionSwitchRatio = Float.valueOf(value);
                    logger.info("set [" + key + "] value: " + value);
                } else if (key.endsWith(KEY_REGION_FORCE_CONFIG)) {
                    initForceRegionConfig(value);
                }
            } catch (Throwable t) {
                logger.warn("set " + key + " failed!", t);
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }
}
