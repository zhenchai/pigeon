package com.dianping.pigeon.remoting.common.util;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;

public interface InvocationBuilder {

	InvocationRequest newRequest();

	InvocationRequest newRequest(InvokerContext invokerContext);

	InvocationRequest newRequest(String serviceName, String methodName, Object[] parameters, byte serialize,
			int messageType, int timeout, Class<?>[] parameterClasses);

	InvocationRequest newRequest(String serviceName, String methodName, Object[] parameters, byte serialize,
			int messageType, int timeout, int callType, long seq);

	Class<? extends InvocationRequest> getRequestClass();

	InvocationResponse newResponse();

	InvocationResponse newResponse(int messageType, byte serialize);

	InvocationResponse newResponse(byte serialize, long seq, int messageType, Object returnVal);

	Class<? extends InvocationResponse> getResponseClass();

	RequestTimeoutException newTimeoutException(String message);

}
