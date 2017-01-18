package com.dianping.pigeon.registry.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenchongze on 17/1/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryConfig {

    private Map<String, ServiceConfig> services = new HashMap<String, ServiceConfig>();

    public Map<String, ServiceConfig> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceConfig> services) {
        this.services = services;
    }
}
