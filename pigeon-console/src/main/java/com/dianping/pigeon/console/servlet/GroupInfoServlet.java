package com.dianping.pigeon.console.servlet;

import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

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
        this.model = RegistryManager.getRegistryConfig();
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
