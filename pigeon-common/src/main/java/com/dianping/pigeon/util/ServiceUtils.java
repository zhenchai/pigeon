package com.dianping.pigeon.util;

import org.apache.commons.lang.StringUtils;

/**
 * Created by chenchongze on 16/4/15.
 */
public class ServiceUtils {

    public static String getServiceId(String serviceName, String suffix) {
        String serviceId = serviceName;
        if (StringUtils.isNotBlank(suffix)) {
            serviceId = serviceId + ":" + suffix;
        }
        return serviceId;
    }
}
