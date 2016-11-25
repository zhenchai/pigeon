package com.dianping.pigeon.remoting.provider.config;

/**
 * Created by chenchongze on 16/11/23.
 */
public enum PoolConfigSource {

    SPRING("spring"),
    CONFIG("config");

    private final String source;

    PoolConfigSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public static PoolConfigSource getPoolConfigSource(String source) {
        switch (source) {
            case "spring":
                return SPRING;
            case "config":
                return CONFIG;
            default:
                return null;
        }
    }
}
