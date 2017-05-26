package com.dianping.pigeon.registry.composite;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 17/5/5.
 */
public class MixUtils {

    private static final Logger logger = LoggerLoader.getLogger(MixUtils.class);

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    public static final String KEY_MIX_MODE_READ_PREFER = "pigeon.registry.mix.mode.read.prefer";

    public static final String KEY_MIX_MODE_FORCE_DOUBLE_WRITE = "pigeon.registry.mix.mode.force.double.write";

    private volatile static String mixReadPrefer = configManager.getStringValue(KEY_MIX_MODE_READ_PREFER, Constants.REGISTRY_MIX_NAME);

    private volatile static boolean mixForceDoubleWrite = configManager.getBooleanValue(KEY_MIX_MODE_FORCE_DOUBLE_WRITE, true);

    // registryName --> registry
    private static final Map<String, Registry> registrys = new HashMap<>();

    // serviceName --> true : mns, false/not exist : curator
    private static final ConcurrentMap<String, Boolean> serviceActives = new ConcurrentHashMap<>();

    // ip:port --> true : mns, false/not exist : curator
    private static final ConcurrentMap<String, Boolean> machineActives = new ConcurrentHashMap<>();

    public static String getMixReadPrefer() {
        return mixReadPrefer;
    }

    public static void setMixReadPrefer(String mixReadPrefer) {
        MixUtils.mixReadPrefer = mixReadPrefer;
    }

    public static boolean isMixForceDoubleWrite() {
        return mixForceDoubleWrite;
    }

    public static void setMixForceDoubleWrite(boolean mixForceDoubleWrite) {
        MixUtils.mixForceDoubleWrite = mixForceDoubleWrite;
    }

    public static Map<String, Registry> getRegistrys() {
        return registrys;
    }

    public static ConcurrentMap<String, Boolean> getServiceActives() {
        return serviceActives;
    }

    public static ConcurrentMap<String, Boolean> getMachineActives() {
        return machineActives;
    }

    public static List<String[]> getServiceIpPortList(String serviceAddress) {
        List<String[]> result = new ArrayList<String[]>();
        if (serviceAddress != null && serviceAddress.length() > 0) {
            String[] hostArray = serviceAddress.split(",");
            for (String host : hostArray) {
                int idx = host.lastIndexOf(":");
                if (idx != -1) {
                    String ip = null;
                    int port = -1;
                    try {
                        ip = host.substring(0, idx);
                        port = Integer.parseInt(host.substring(idx + 1));
                    } catch (RuntimeException e) {
                        logger.warn("invalid host: " + host + ", ignored!");
                    }
                    if (ip != null && port > 0) {
                        result.add(new String[]{ip, port + ""});
                    }
                } else {
                    logger.warn("invalid host: " + host + ", ignored!");
                }
            }
        }
        return result;
    }

    public static String getIp(String host) {
        int idx = host.lastIndexOf(":");
        if (idx != -1) {
            try {
                return host.substring(0, idx);
            } catch (RuntimeException e) {
                // ignore
            }
        }

        return null;
    }
}
