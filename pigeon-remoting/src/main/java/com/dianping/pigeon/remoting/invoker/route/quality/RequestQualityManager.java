package com.dianping.pigeon.remoting.invoker.route.quality;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenchongze on 16/5/20.
 */
public enum RequestQualityManager {

    INSTANCE;

    private RequestQualityManager() {
        ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
    }

    private static final Logger logger = LoggerLoader.getLogger(RequestQualityManager.class);

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private static final String KEY_REQUEST_QUALITY_AUTO = "pigeon.invoker.request.quality.auto.active";
    private static final String KEY_REQUEST_QUALITY_FAILED_PERCENT_GOOD = "pigeon.invoker.request.quality.failed.percent.good";
    private static final String KEY_REQUEST_QUALITY_FAILED_PERCENT_NORMAL = "pigeon.invoker.request.quality.failed.percent.normal";
    private static final String KEY_REQUEST_QUALITY_THRESHOLD_TOTAL = "pigeon.invoker.request.quality.threshold.total";
    private volatile static boolean isReqQualityEnable = configManager.getBooleanValue(KEY_REQUEST_QUALITY_AUTO, false);
    private volatile static Float reqQualityFailedPercentGood = configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_GOOD, 1f);
    private volatile static Float reqQualityFailedPercentNormal = configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_NORMAL, 5f);
    private volatile static int reqQualityThresholdTotal = configManager.getIntValue(KEY_REQUEST_QUALITY_THRESHOLD_TOTAL, 20);

    // hosts --> ( requestUrl:serviceName#method --> second --> { total, failed } )
    private ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<Integer, Quality>>>
            addrReqUrlSecondQualities = new ConcurrentHashMap<String, ConcurrentMap<String, ConcurrentMap<Integer, Quality>>>();

    // hosts --> ( requestUrl:serviceName#method --> { total, failed } )
    private volatile ConcurrentMap<String, ConcurrentMap<String, Quality>> addrReqUrlQualities = null;

    public ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<Integer, Quality>>> getAddrReqUrlSecondQualities() {
        return addrReqUrlSecondQualities;
    }

    public ConcurrentMap<String, ConcurrentMap<String, Quality>> getAddrReqUrlQualities() {
        return addrReqUrlQualities;
    }

    public void setAddrReqUrlQualities(ConcurrentMap<String, ConcurrentMap<String, Quality>> addrReqUrlQualities) {
        this.addrReqUrlQualities = addrReqUrlQualities;
    }

    public void addClientRequest(InvokerContext context, boolean failed) {
        if (isReqQualityEnable && context.getClient() != null) {

            String address = context.getClient().getAddress();
            ConcurrentMap<String, ConcurrentMap<Integer, Quality>>
                    requestSecondQuality = addrReqUrlSecondQualities.get(address);
            if (requestSecondQuality == null) {
                requestSecondQuality = new ConcurrentHashMap<String, ConcurrentMap<Integer, Quality>>();
                ConcurrentMap<String, ConcurrentMap<Integer, Quality>>
                        last = addrReqUrlSecondQualities.putIfAbsent(address, requestSecondQuality);
                if (last != null) {
                    requestSecondQuality = last;
                }
            }

            String requestUrl = getRequestUrl(context);
            ConcurrentMap<Integer, Quality> secondQuality = requestSecondQuality.get(requestUrl);
            if (secondQuality == null) {
                secondQuality = new ConcurrentHashMap<Integer, Quality>();
                ConcurrentMap<Integer, Quality> last = requestSecondQuality.putIfAbsent(requestUrl, secondQuality);
                if (last != null) {
                    secondQuality = last;
                }
            }

            int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
            Quality quality = secondQuality.get(currentSecond);
            if (quality == null) {
                quality = new Quality(0, 0);
                Quality last = secondQuality.putIfAbsent(currentSecond, quality);
                if (last != null) {
                    quality = last;
                }
            }

            quality.total.incrementAndGet();
            if (failed) {
                quality.failed.incrementAndGet();
            }
        }
    }

    public void removeClientQualities(String address) {
        addrReqUrlSecondQualities.remove(address);
    }

    private String getRequestUrl(InvokerContext context) {
        return context.getInvokerConfig().getUrl() + "#" + context.getMethodName();
    }

    private String getRequestUrl(InvocationRequest request) {
        return request.getServiceName() + "#" + request.getMethodName();
    }

    /**
     * 根据方法的服务质量过滤，优先保留服务质量good的clients，数量低于least时加入服务质量normal的clients
     * 存在的问题,如果一个节点是个死节点,那么他永远是优秀的节点
     *
     * @param clientList
     * @param request
     * @param least      最少保留个数
     * @return
     */
    @Deprecated
    public List<Client> getQualityPreferClients(List<Client> clientList, InvocationRequest request, float least) {
        // 筛选good，normal，bad clients
        // 直接进行服务质量路由,先只保留服务质量good的，如果不够（比如少于1个），加入服务质量normal的
        if (!CollectionUtils.isEmpty(addrReqUrlQualities)) {
            String requestUrl = getRequestUrl(request);

            Map<RequrlQuality, List<Client>> filterQualityClientsMap = new HashMap<RequrlQuality, List<Client>>();
            for (RequrlQuality reqQuality : RequrlQuality.values()) {
                filterQualityClientsMap.put(reqQuality, new ArrayList<Client>());
            }

            for (Client client : clientList) {
                if (addrReqUrlQualities.containsKey(client.getAddress())) {
                    ConcurrentMap<String, Quality> reqUrlQualities = addrReqUrlQualities.get(client.getAddress());
                    if (reqUrlQualities.containsKey(requestUrl)) {
                        Quality quality = reqUrlQualities.get(requestUrl);

                        switch (quality.getQuality()) {
                            case REQURL_QUALITY_GOOD:
                                filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_GOOD).add(client);
                                break;
                            case REQURL_QUALITY_NORNAL:
                                filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_NORNAL).add(client);
                                break;
                            case REQURL_QUALITY_BAD:
                                filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_BAD).add(client);
                                break;
                            default:
                                // never be here
                                break;
                        }
                    }
                }
            }

            List<Client> filterQualityClients = new ArrayList<Client>();
            filterQualityClients.addAll(filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_GOOD));

            if (filterQualityClients.size() < least) {
                filterQualityClients.addAll(filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_NORNAL));
            }

            return filterQualityClients;
        }

        return clientList;
    }

    public boolean isEnableRequestQualityRoute() {
        return isReqQualityEnable;
    }

    public int adjustWeightWithQuality(int weight, String clientAddress, InvocationRequest request) {
        if (isEnableRequestQualityRoute()) {
            if (!CollectionUtils.isEmpty(addrReqUrlQualities)) {
                ConcurrentMap<String, Quality> reqUrlQualities = addrReqUrlQualities.get(clientAddress);
                if (reqUrlQualities != null) {
                    Quality quality = reqUrlQualities.get(getRequestUrl(request));
                    if (quality != null) {
                        weight /= quality.getQualityValue(); // int 多数会归零
                    }
                }
            }
        }

        return weight;
    }

    public static class Quality {

        private RequrlQuality quality = RequrlQuality.REQURL_QUALITY_GOOD;
        private AtomicInteger failed = new AtomicInteger();
        private AtomicInteger total = new AtomicInteger();

        public Quality() {
        }

        public Quality(int total, int failed) {
            this.total.set(total);
            this.failed.set(failed);
        }

        public AtomicInteger getFailed() {
            return failed;
        }

        public int getFailedValue() {
            return failed.get();
        }

        public void setFailed(int failed) {
            this.failed.set(failed);
        }

        public AtomicInteger getTotal() {
            return total;
        }

        public int getTotalValue() {
            return total.get();
        }

        public void setTotal(int total) {
            this.total.set(total);
        }

        public float getFailedPercent() {
            if (total.get() > 0) {
                return failed.get() * 100 / total.get();
            } else {
                return 0;
            }
        }

        public void clear() {
            total.set(0);
            failed.set(0);
            quality = RequrlQuality.REQURL_QUALITY_GOOD;
        }

        public RequrlQuality getQuality() {
            if (getTotalValue() > reqQualityThresholdTotal) {
                float failedRate = getFailedPercent();

                if (failedRate < reqQualityFailedPercentGood) {
                    quality = RequrlQuality.REQURL_QUALITY_GOOD;
                } else if (failedRate >= reqQualityFailedPercentGood && failedRate < reqQualityFailedPercentNormal) {
                    quality = RequrlQuality.REQURL_QUALITY_NORNAL;
                } else if (failedRate >= reqQualityFailedPercentNormal) {
                    quality = RequrlQuality.REQURL_QUALITY_BAD;
                }
            }

            return quality;
        }

        public int getQualityValue() {
            return getQuality().getValue();
        }
    }

    private enum RequrlQuality {
        REQURL_QUALITY_GOOD(1),
        REQURL_QUALITY_NORNAL(10),
        REQURL_QUALITY_BAD(100);

        private int value;

        RequrlQuality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {
        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith(KEY_REQUEST_QUALITY_AUTO)) {
                try {
                    isReqQualityEnable = Boolean.valueOf(value);
                    logger.info("set request quality switch to " + value);
                } catch (RuntimeException e) {
                    logger.warn("set request quality switch failed!", e);
                }
            } else if (key.endsWith(KEY_REQUEST_QUALITY_FAILED_PERCENT_GOOD)) {
                try {
                    reqQualityFailedPercentGood = Float.valueOf(value);
                    logger.info("set req quality failed percent good to " + value);
                } catch (RuntimeException e) {
                    logger.warn("set req quality failed percent good failed!", e);
                }
            } else if (key.endsWith(KEY_REQUEST_QUALITY_FAILED_PERCENT_NORMAL)) {
                try {
                    reqQualityFailedPercentNormal = Float.valueOf(value);
                    logger.info("set req quality failed percent normal to " + value);
                } catch (RuntimeException e) {
                    logger.warn("set req quality failed percent normal failed!", e);
                }
            } else if (key.endsWith(KEY_REQUEST_QUALITY_THRESHOLD_TOTAL)) {
                try {
                    reqQualityThresholdTotal = Integer.valueOf(value);
                    logger.info("set req quality threshold total to " + value);
                } catch (RuntimeException e) {
                    logger.warn("set req quality threshold total failed!", e);
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
