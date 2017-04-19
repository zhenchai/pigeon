package com.dianping.pigeon.remoting.invoker.route.region;

/**
 * Created by chenchongze on 17/4/12.
 */
public interface RegionHelper {

    String getLocalIdc();

    boolean isInLocalIdc(String ip);
}
