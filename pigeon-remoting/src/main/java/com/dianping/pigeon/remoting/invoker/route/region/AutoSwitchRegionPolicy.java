package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
import com.google.common.collect.Lists;
import com.dianping.pigeon.log.Logger;

import java.util.*;

/**
 * Created by chenchongze on 16/4/15.
 */
public class AutoSwitchRegionPolicy implements RegionPolicy {

    public final static AutoSwitchRegionPolicy INSTANCE = new AutoSwitchRegionPolicy();

    public static final String NAME = "autoSwitch";

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;

    private final RequestQualityManager requestQualityManager = RequestQualityManager.INSTANCE;

    private final RegistryManager registryManager = RegistryManager.getInstance();

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private final Monitor monitor = MonitorLoader.getMonitor();

    private static final String KEY_REGION_THRESHOLD_RATIO = "pigeon.regions.switchratio";
    private static final String KEY_IDC_FILTER_ENABLE = "pigeon.regions.autoswitch.idc.enable";
    private static final String KEY_IDC_FILTER_THRESHOLD_LEAST = "pigeon.regions.autoswitch.idc.threshold.least";
    private static final String KEY_IDC_FILTER_THRESHOLD_RATIO = "pigeon.regions.autoswitch.idc.threshold.ratio";

    private volatile float regionSwitchRatio = configManager.getFloatValue(KEY_REGION_THRESHOLD_RATIO, 0.5f);
    private volatile boolean isIdcFilterEnable = configManager.getBooleanValue(KEY_IDC_FILTER_ENABLE, false);
    private volatile int idcFilterThresholdLeast = configManager.getIntValue(KEY_IDC_FILTER_THRESHOLD_LEAST, 2);
    private volatile float idcFilterThresHoldRatio = configManager.getFloatValue(KEY_IDC_FILTER_THRESHOLD_RATIO, 0.2f);

    private AutoSwitchRegionPolicy() {
        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
    }

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvocationRequest request) {
        return getRegionActiveClients(clientList, request);
    }

    private List<Client> getRegionActiveClients(List<Client> clientList, InvocationRequest request) {
        int sizeBefore = clientList.size();

        Map<Region, InnerRegionStat> regionStats = new HashMap<Region, InnerRegionStat>();
        List<Region> regionArrays = Lists.newArrayList(regionPolicyManager.getRegionArray());

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

                if (i == 0 && isIdcFilterEnable && regionClientList.size() > 0) { // 开启本地idc(region(0))优先
                    int idcTotal = 0;
                    int idcActive = 0;
                    List<Client> idcClientList = new ArrayList<Client>();
                    for (Client client : regionClientList) {
                        if (RegionUtils.isInLocalIdc(client.getHost())) {
                            idcClientList.add(client);
                            ++ idcTotal;
                            if (client.isActive() && registryManager.getServiceWeightFromCache(client.getAddress()) > 0) {
                                ++ idcActive;
                            }
                        }
                    }

                    float idcLeast = idcFilterThresHoldRatio * idcTotal;

                    if (idcTotal > 0 && idcActive > 0 && idcActive >= idcLeast) { // idc可用client比例合格
                        if (idcActive > idcFilterThresholdLeast || idcActive > idcFilterThresHoldRatio * active) {
                            // idc可用client数量合格 或 idc可用client数量占region可用client数量的比例合格
                            monitor.logEvent("PigeonCall.idc",
                                    request.getServiceName() + "#" + RegionUtils.getLocalIdc(), "");
                            return idcClientList;
                        }
                    }
                }

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
                    monitor.logEvent("PigeonCall.regionUnavailable",
                            request.getServiceName() + "#" + region.getName(), "");
                }
            } catch (Throwable t) {
                logger.error(t);
            } finally {
                //todo if force region, maybe here
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
            if (key.endsWith(KEY_IDC_FILTER_ENABLE)) {
                try {
                    isIdcFilterEnable = Boolean.valueOf(value);
                    logger.info("set " + KEY_IDC_FILTER_ENABLE + " value: " + value);
                } catch (RuntimeException e) {
                    logger.warn("set " + KEY_IDC_FILTER_ENABLE + " failed!", e);
                }
            } else if (key.endsWith(KEY_IDC_FILTER_THRESHOLD_LEAST)) {
                try {
                    idcFilterThresholdLeast = Integer.valueOf(value);
                    logger.info("set " + KEY_IDC_FILTER_THRESHOLD_LEAST + " value: " + value);
                } catch (RuntimeException e) {
                    logger.warn("set " + KEY_IDC_FILTER_THRESHOLD_LEAST + " failed!", e);
                }
            } else if (key.endsWith(KEY_IDC_FILTER_THRESHOLD_RATIO)) {
                try {
                    idcFilterThresHoldRatio = Float.valueOf(value);
                    logger.info("set " + KEY_IDC_FILTER_THRESHOLD_RATIO + " value: " + value);
                } catch (RuntimeException e) {
                    logger.warn("set " + KEY_IDC_FILTER_THRESHOLD_RATIO + " failed!", e);
                }
            } else if (key.endsWith(KEY_REGION_THRESHOLD_RATIO)) {
                try {
                    regionSwitchRatio = Float.valueOf(value);
                    logger.info("set " + KEY_REGION_THRESHOLD_RATIO + " value: " + value);
                } catch (RuntimeException e) {
                    logger.warn("set " + KEY_REGION_THRESHOLD_RATIO + " failed!", e);
                }
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
