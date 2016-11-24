package com.dianping.pigeon.console.domain;


import java.util.List;

/**
 * Created by shihuashen on 16/10/24.
 */
public class ServicePaths {
    private List<ServicePath> providerPaths;
    private List<ServicePath> invokerPaths;

    public List<ServicePath> getProviderPaths() {
        return providerPaths;
    }

    public void setProviderPaths(List<ServicePath> providerPaths) {
        this.providerPaths = providerPaths;
    }

    public List<ServicePath> getInvokerPaths() {
        return invokerPaths;
    }

    public void setInvokerPaths(List<ServicePath> invokerPaths) {
        this.invokerPaths = invokerPaths;
    }
}
