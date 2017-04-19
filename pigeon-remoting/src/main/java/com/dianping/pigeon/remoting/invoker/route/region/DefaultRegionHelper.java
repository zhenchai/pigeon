package com.dianping.pigeon.remoting.invoker.route.region;

/**
 * Created by chenchongze on 17/4/12.
 */
public class DefaultRegionHelper implements RegionHelper {


    @Override
    public String getLocalIdc() {
        return null;
    }

    @Override
    public boolean isInLocalIdc(String ip) {
        return false;
    }
}
