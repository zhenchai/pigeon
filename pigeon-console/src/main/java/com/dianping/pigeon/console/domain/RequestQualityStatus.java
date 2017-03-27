package com.dianping.pigeon.console.domain;

import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenchongze on 16/6/29.
 */
public class RequestQualityStatus {

    private Boolean support = Boolean.FALSE;

    private ConcurrentMap<String, ConcurrentMap<String, RequestQualityManager.Quality>> addrReqUrlQualities
            = new ConcurrentHashMap<String, ConcurrentMap<String, RequestQualityManager.Quality>>();

    public Boolean getSupport() {
        return support;
    }

    public void setSupport(Boolean support) {
        this.support = support;
    }

    public ConcurrentMap<String, ConcurrentMap<String, RequestQualityManager.Quality>> getAddrReqUrlQualities() {
        return addrReqUrlQualities;
    }

    public void setAddrReqUrlQualities(ConcurrentMap<String, ConcurrentMap<String, RequestQualityManager.Quality>> addrReqUrlQualities) {
        this.addrReqUrlQualities = addrReqUrlQualities;
    }
}
