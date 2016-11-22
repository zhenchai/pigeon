package com.dianping.pigeon.remoting.common.domain;

/**
 * @author qi.yin
 *         2016/11/14  下午7:38.
 */
public enum MessageType {
    HEART((byte) 1, "heart"),

    SERVICE((byte) 2, "service"),

    EXCEPTION((byte) 3, "exception"),

    SERVICE_EXCEPTION((byte) 4, "serviceException"),

    HEALTH_CHECKER((byte) 5, "healthChecker"),

    SCANNER_HEART((byte) 6, "scannerHeart");

    private byte code;
    private String name;

    private MessageType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MessageType getMessageType(byte code) {
        switch (code) {
            case 1:
                return HEART;
            case 2:
                return SERVICE;
            case 3:
                return EXCEPTION;
            case 4:
                return SERVICE_EXCEPTION;
            case 5:
                return HEALTH_CHECKER;
            case 6:
                return SCANNER_HEART;
            default:
                throw new IllegalArgumentException("invalid messageType code: " + code);
        }
    }

    public static boolean isHeart(byte code) {
        return HEART.getCode() == code;
    }

    public static boolean isService(byte code) {
        return SERVICE.getCode() == code;
    }

    public static boolean isException(byte code) {
        return EXCEPTION.getCode() == code;
    }

    public static boolean isServiceException(byte code) {
        return SERVICE_EXCEPTION.getCode() == code;
    }

    public static boolean isHealthChecker(byte code) {
        return HEALTH_CHECKER.getCode() == code;
    }

    public static boolean isScannerHeart(byte code) {
        return SCANNER_HEART.getCode() == code;
    }
}