package com.dianping.pigeon.remoting.common.codec.protobuf;

import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.domain.DefaultRequest;
import com.dianping.pigeon.remoting.common.domain.DefaultResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.common.util.ContextUtils;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchongze on 17/3/7.
 */
public class Protobuf3Serializer extends AbstractSerializer {

    private static Objenesis objenesis = new ObjenesisStd(true);

    public Protobuf3Serializer() {
    }

    @Override
    public Object deserializeRequest(InputStream is) throws SerializationException {
        try {
            Entities.Pb3Request pb3Request = Entities.Pb3Request.parseFrom(is);

            List<Any> parametersList = pb3Request.getParametersList();
            Object[] parameters = new Object[parametersList.size()];

            for (int i = 0; i < parametersList.size(); i++) {
                Any paramAny = parametersList.get(i);
                if (StringUtils.isBlank(paramAny.getTypeUrl())) {
                    parameters[i] = null;
                } else {
                    parameters[i] = unpack(paramAny);
                }
            }

            DefaultRequest request = new DefaultRequest(
                    pb3Request.getServiceName(),
                    pb3Request.getMethodName(),
                    parameters,
                    (byte)pb3Request.getSerialize(),
                    pb3Request.getMessageType(),
                    pb3Request.getTimeout(),
                    pb3Request.getCallType(),
                    pb3Request.getSeq());

            request.setVersion(pb3Request.getVersion());
            request.setApp(pb3Request.getApp());
            request.setGlobalValues(ContextUtils.convertContext(pb3Request.getGlobalValuesMap()));
            request.setRequestValues(ContextUtils.convertContext(pb3Request.getRequestValuesMap()));

            return request;
        } catch (Throwable t) {
            throw new SerializationException(t.getMessage(), t);
        }
    }

    @Override
    public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
        try {
            InvocationRequest invocationRequest = (InvocationRequest) obj;
            Map<String, String> globalValues = new HashMap<String, String>();
            ContextUtils.convertContext(invocationRequest.getGlobalValues(), globalValues);
            Map<String, String> requestValues = new HashMap<String, String>();
            ContextUtils.convertContext(invocationRequest.getRequestValues(), requestValues);

            Entities.Pb3Request.Builder requestBuilder = Entities.Pb3Request.newBuilder()
                    .setSerialize(invocationRequest.getSerialize())
                    .setSeq(invocationRequest.getSequence())
                    .setMessageType(invocationRequest.getMessageType())
                    .setTimeout(invocationRequest.getTimeout())
                    .setServiceName(invocationRequest.getServiceName())
                    .setMethodName(invocationRequest.getMethodName())
                    .setCallType(invocationRequest.getCallType())
                    .setVersion(invocationRequest.getVersion() == null ? "" : invocationRequest.getVersion())
                    .setApp(invocationRequest.getApp() == null ? "" : invocationRequest.getApp())
                    .putAllGlobalValues(globalValues)
                    .putAllRequestValues(requestValues);

            if (invocationRequest.getParameters() != null) {
                for (Object param : invocationRequest.getParameters()) {
                    if (param == null) {
                        requestBuilder.addParameters(Any.getDefaultInstance());
                    } else {
                        requestBuilder.addParameters(pack((Message) param));
                    }
                }
            }


            requestBuilder.build().writeTo(os);
        } catch (Throwable t) {
            throw new SerializationException(t.getMessage(), t);
        }
    }

    @Override
    public Object deserializeResponse(InputStream is) throws SerializationException {
        try {
            Entities.Pb3Response pb3Response = Entities.Pb3Response.parseFrom(is);

            Object returnVal = null;
            Any returnValAny = pb3Response.getReturnVal();
            if (StringUtils.isNotBlank(returnValAny.getTypeUrl())) {
                returnVal = unpack(returnValAny);
            } else if (StringUtils.isNotBlank(pb3Response.getException().getCause())) {
                returnVal = objenesis.newInstance(Class.forName(pb3Response.getException().getCause()));
                Field msgField = Throwable.class.getDeclaredField("detailMessage");
                msgField.setAccessible(true);
                msgField.set(returnVal, pb3Response.getException().getDetailMessage());
                msgField.setAccessible(false);
            }

            DefaultResponse response = new DefaultResponse(
                    (byte)pb3Response.getSerialize(),
                    pb3Response.getSeq(),
                    pb3Response.getMessageType(),
                    returnVal,
                    pb3Response.getCause());
            response.setResponseValues(ContextUtils.convertContext(pb3Response.getResponseValuesMap()));

            return response;
        } catch (Throwable t) {
            throw new SerializationException(t.getMessage(), t);
        }
    }

    @Override
    public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
        try {
            InvocationResponse invocationResponse = (InvocationResponse) obj;
            Map<String, String> responseValues = new HashMap<String, String>();
            ContextUtils.convertContext(invocationResponse.getResponseValues(), responseValues);

            Entities.Pb3Response.Builder responseBuilder = Entities.Pb3Response.newBuilder()
                    .setSerialize(invocationResponse.getSerialize())
                    .setSeq(invocationResponse.getSequence())
                    .setMessageType(invocationResponse.getMessageType())
                    .setCause(invocationResponse.getCause() == null ? "" : invocationResponse.getCause())
                    .putAllResponseValues(responseValues);

            // exception or normal
            Object returnVal = invocationResponse.getReturn();
            if (returnVal != null) {
                if (returnVal instanceof Message) {
                    responseBuilder.setReturnVal(pack((Message) returnVal));
                } else if (returnVal instanceof Throwable) {
                    responseBuilder.setException(
                            Entities.Pb3Exception.newBuilder()
                                    .setCause(returnVal.getClass().getName())
                                    .setDetailMessage(ExceptionUtils.getStackTrace((Throwable) returnVal))
                    );
                } else {
                    throw new RuntimeException("return val must be a Message or Exception! return class: "
                            + returnVal.getClass().getName());
                }
            }

            responseBuilder.build().writeTo(os);
        } catch (Throwable t) {
            throw new SerializationException(t.getMessage(), t);
        }
    }

    private static Any pack(Message msg) {
        return Any.newBuilder().setTypeUrl(msg.getClass().getName())
                .setValue(msg.toByteString()).build();
    }

    private static Object unpack(Any any) {
        Object obj;
        try {
            Class clazz = Class.forName(any.getTypeUrl());
            Method method = clazz.getMethod("getDefaultInstance");
            obj = method.invoke(method);
            return ((Message) obj).getParserForType().parseFrom(any.getValue());
        } catch (Throwable t) {
            throw new RuntimeException("Failed to unpack " + any, t);
        }
    }
}
