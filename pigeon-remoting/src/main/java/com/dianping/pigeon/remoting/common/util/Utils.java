package com.dianping.pigeon.remoting.common.util;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchongze on 17/1/3.
 */
public class Utils {

    private static final Logger logger = LoggerLoader.getLogger(Utils.class);

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
}
