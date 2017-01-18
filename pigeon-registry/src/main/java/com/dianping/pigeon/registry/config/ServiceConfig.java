package com.dianping.pigeon.registry.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by chenchongze on 17/1/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceConfig {

    private String group = "";

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
