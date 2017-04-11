package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;

import java.util.List;

/**
 * Created by chenchongze on 17/4/11.
 */
public class IdcAutoSwitchRegionPolicy implements RegionPolicy {

    public final static IdcAutoSwitchRegionPolicy INSTANCE = new IdcAutoSwitchRegionPolicy();

    public static final String NAME = "IdcAutoSwitch";

    private final Logger logger = LoggerLoader.getLogger(this.getClass());

    private final RegionPolicyManager regionPolicyManager = RegionPolicyManager.INSTANCE;

    private IdcAutoSwitchRegionPolicy () {}

    @Override
    public List<Client> getPreferRegionClients(List<Client> clientList, InvocationRequest request) {


        return null;
    }
}
