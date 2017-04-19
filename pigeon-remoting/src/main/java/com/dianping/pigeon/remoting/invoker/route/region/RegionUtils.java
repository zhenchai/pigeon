package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.extension.ExtensionLoader;

/**
 * Created by chenchongze on 17/4/12.
 */
public class RegionUtils {

    private static RegionHelper helper;

    static {
        helper = ExtensionLoader.getExtension(RegionHelper.class);
        if (helper == null) {
            helper = new DefaultRegionHelper();
        }
    }

    public static String getLocalIdc() {
        return helper.getLocalIdc();
    }

    public static boolean isInLocalIdc(String ip) {
        return helper.isInLocalIdc(ip);
    }
}
