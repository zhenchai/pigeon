package com.dianping.pigeon.remoting.common.domain;

/**
 * @author qi.yin
 *         2016/11/09  上午11:01.
 */
public enum CallType {
    REPLY((byte) 1, "reply"), NOREPLY((byte) 2, "noreply"), MANUAL((byte) 3, "manual");


    private byte code;

    private String name;

    private CallType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static CallType getCallType(byte code) {
        switch (code) {
            case 1:
                return REPLY;
            case 2:
                return NOREPLY;
            case 3:
                return MANUAL;
            default:
                throw new IllegalArgumentException("invalid callType code: " + code);

        }
    }

    public static boolean isReply(byte code) {
        return REPLY.getCode() == code;
    }

    public static boolean isReply(String name) {
        return REPLY.getName().equals(name);
    }

    public static boolean isNoReply(byte code) {
        return NOREPLY.getCode() == code;
    }

    public static boolean isNoReply(String name) {
        return NOREPLY.getName().equals(name);
    }

    public static boolean isManual(byte code) {
        return MANUAL.getCode() == code;
    }

    public static boolean isManual(String name) {
        return MANUAL.getName().equals(name);
    }
}
