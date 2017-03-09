package com.dianping.pigeon.remoting.common.codec;

/**
 * @author qi.yin
 *         2016/11/04  下午7:41.
 */
public enum SerializerType {

    INTERNAL_THRIFT((byte) 1, "internalThrift"),

    HESSIAN((byte) 2, "hessian"),

    JAVA((byte) 3, "java"),

    PROTO((byte) 5, "proto"),

    HESSIAN1((byte) 6, "hessian1"),

    JSON((byte) 7, "json"),

    FST((byte) 8, "fst"),

    PROTOBUF((byte) 9, "protobuf"),

    THRIFT((byte) 10, "thrift"),

    PROTOBUF3((byte) 11, "protobuf3");

    private byte code;
    private String name;

    private SerializerType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public static SerializerType getSerializerType(byte code) {
        switch (code) {
            case 1:
                return INTERNAL_THRIFT;
            case 2:
                return HESSIAN;
            case 3:
                return JAVA;
            case 4:
                throw new IllegalArgumentException("invalid serializerType code: " + code);
            case 5:
                return PROTO;
            case 6:
                return HESSIAN1;
            case 7:
                return JSON;
            case 8:
                return FST;
            case 9:
                return PROTOBUF;
            case 10:
                return THRIFT;
            case 11:
                return PROTOBUF3;
            default:
                throw new IllegalArgumentException("invalid serializerType code: " + code);

        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public static boolean isInternalThrift(byte code) {
        return INTERNAL_THRIFT.getCode() == code;
    }

    public static boolean isHessian(byte code) {
        return HESSIAN.getCode() == code;
    }

    public static boolean isJava(byte code) {
        return JAVA.getCode() == code;
    }

    public static boolean isProto(byte code) {
        return PROTO.getCode() == code;
    }

    public static boolean isHessian1(byte code) {
        return HESSIAN1.getCode() == code;
    }

    public static boolean isJson(byte code) {
        return JSON.getCode() == code;
    }

    public static boolean isFst(byte code) {
        return FST.getCode() == code;
    }

    public static boolean isProtobuf(byte code) {
        return PROTOBUF.getCode() == code;
    }

    public static boolean isThrift(byte code) {
        return THRIFT.getCode() == code;
    }

    public static boolean isProtobuf3(byte code) {
        return PROTOBUF3.getCode() == code;
    }

    public static boolean isInternalThrift(String desc) {
        return INTERNAL_THRIFT.getName().equals(desc);
    }

    public static boolean isHessian(String desc) {
        return HESSIAN.getName().equals(desc);
    }

    public static boolean isJava(String desc) {
        return JAVA.getName().equals(desc);
    }

    public static boolean isProto(String desc) {
        return PROTO.getName().equals(desc);
    }

    public static boolean isHessian1(String desc) {
        return HESSIAN1.getName().equals(desc);
    }

    public static boolean isJson(String desc) {
        return JSON.getName().equals(desc);
    }

    public static boolean isFst(String desc) {
        return FST.getName().equals(desc);
    }

    public static boolean isProtobuf(String desc) {
        return PROTOBUF.getName().equals(desc);
    }

    public static boolean isThrift(String desc) {
        return THRIFT.getName().equals(desc);
    }

    public static boolean isProtobuf3(String desc) {
        return PROTOBUF3.getName().equals(desc);
    }
    
}
