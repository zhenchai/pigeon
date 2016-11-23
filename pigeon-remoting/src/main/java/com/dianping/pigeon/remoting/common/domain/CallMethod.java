package com.dianping.pigeon.remoting.common.domain;

/**
 * @author qi.yin
 *         2016/11/09  上午11:18.
 */
public enum CallMethod {

    SYNC((byte) 1, "sync"), CALLBACK((byte) 2, "callback"), FUTURE((byte) 3, "future"), ONEWAY((byte) 4, "oneway");

    private byte code;
    private String name;

    private CallMethod(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static CallMethod getCallMethod(byte code) {
        switch (code) {
            case 1:
                return SYNC;
            case 2:
                return CALLBACK;
            case 3:
                return FUTURE;
            case 4:
                return ONEWAY;
            default:
                throw new IllegalArgumentException("invalid callMethod code: " + code);

        }
    }

    public static CallMethod getCallMethod(String name) {
        if (SYNC.isSync(name)) {
            return SYNC;
        } else if (CALLBACK.isCallback(name)) {
            return CALLBACK;
        } else if (FUTURE.isFuture(name)) {
            return FUTURE;
        } else if (ONEWAY.isOneway(name)) {
            return ONEWAY;
        } else {
            throw new IllegalArgumentException("invalid callMethod name: " + name);
        }
    }

    public static boolean isSync(byte code) {
        return SYNC.getCode() == code;
    }

    public static boolean isSync(String name) {
        if (name == null) {
            return false;
        }
        return SYNC.getName().equals(name.toLowerCase());
    }

    public static boolean isCallback(byte code) {
        return CALLBACK.getCode() == code;
    }

    public static boolean isCallback(String name) {
        if (name == null) {
            return false;
        }
        return CALLBACK.getName().equals(name.toLowerCase());
    }

    public static boolean isFuture(byte code) {
        return FUTURE.getCode() == code;
    }

    public static boolean isFuture(String name) {
        if (name == null) {
            return false;
        }
        return FUTURE.getName().equals(name.toLowerCase());
    }

    public static boolean isOneway(byte code) {
        return ONEWAY.getCode() == code;
    }

    public static boolean isOneway(String name) {
        if (name == null) {
            return false;
        }
        return ONEWAY.getName().equals(name.toLowerCase());
    }
}
