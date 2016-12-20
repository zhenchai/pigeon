package com.dianping.pigeon.registry.util;

/**
 * Created by chenchongze on 16/8/16.
 */
public enum HeartBeatSupport {

    NoSupport((byte)0),
    P2POnly((byte)1),
    ScannerOnly((byte)2),
    BothSupport((byte)3);

    private final byte value;

    private HeartBeatSupport(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static HeartBeatSupport findByValue(byte value) {
        switch(value) {
            case 0:
                return NoSupport;
            case 1:
                return P2POnly;
            case 2:
                return ScannerOnly;
            case 3:
                return BothSupport;
            default:
                return BothSupport;
        }
    }
}
