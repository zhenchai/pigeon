package com.dianping.pigeon.console.servlet;

import com.dianping.pigeon.console.domain.GroupInfo;
import com.dianping.pigeon.registry.route.GroupManager;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by chenchongze on 17/1/12.
 */
public class GroupInfoServlet extends ServiceServlet {

    public GroupInfoServlet(ServerConfig serverConfig, int port) {
        super(serverConfig, port);
    }

    @Override
    protected boolean initServicePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setInvokerGroupCache(GroupManager.INSTANCE.getInvokerGroupCache());
        groupInfo.setProviderGroupCache(GroupManager.INSTANCE.getProviderGroupCache());
        this.model = groupInfo;
        return true;
    }

    @Override
    public String getView() {
        return "GroupInfo.ftl";
    }

    @Override
    public String getContentType() {
        return "application/json; charset=UTF-8";
    }
}
