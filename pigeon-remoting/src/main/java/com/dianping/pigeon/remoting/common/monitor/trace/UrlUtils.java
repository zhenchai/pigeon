package com.dianping.pigeon.remoting.common.monitor.trace;

import org.apache.commons.lang.StringUtils;

/**
 * Created by andy on 17/6/15.
 */
public class UrlUtils {

    public static boolean url(String url) {
        if (StringUtils.isNotBlank(url) && url.startsWith("/")) {
            return true;
        }
        return false;
    }
}
