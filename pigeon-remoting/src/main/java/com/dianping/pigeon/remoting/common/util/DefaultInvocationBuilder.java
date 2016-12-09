package com.dianping.pigeon.remoting.common.util;

import com.dianping.pigeon.remoting.common.domain.DefaultRequest;
import com.dianping.pigeon.remoting.common.domain.DefaultResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;

public class DefaultInvocationBuilder implements InvocationBuilder {

	@Override
	public InvocationRequest newRequest() {
		return new DefaultRequest();
	}

	@Override
	public InvocationRequest newRequest(InvokerContext invokerContext) {
		return new DefaultRequest(invokerContext);
	}

	@Override
	public InvocationRequest newRequest(String serviceName, String methodName, Object[] parameters, byte serialize,
			int messageType, int timeout, Class<?>[] parameterClasses) {
		return new DefaultRequest(serviceName, methodName, parameters, serialize, messageType, timeout, parameterClasses);
	}

	@Override
	public InvocationRequest newRequest(String serviceName, String methodName, Object[] parameters, byte serialize,
			int messageType, int timeout, int callType, long seq) {
		return new DefaultRequest(serviceName, methodName, parameters, serialize, messageType, timeout, callType, seq);
	}

	@Override
	public Class<? extends InvocationRequest> getRequestClass() {
		return DefaultRequest.class;
	}

	@Override
	public InvocationResponse newResponse() {
		return new DefaultResponse();
	}

	@Override
	public InvocationResponse newResponse(int messageType, byte serialize) {
		return new DefaultResponse(messageType, serialize);
	}

	@Override
	public InvocationResponse newResponse(byte serialize, long seq, int messageType, Object returnVal) {
		return new DefaultResponse(serialize, seq, messageType, returnVal);
	}

	@Override
	public Class<? extends InvocationResponse> getResponseClass() {
		return DefaultResponse.class;
	}

	@Override
	public RequestTimeoutException newTimeoutException(String message) {
		return new RequestTimeoutException(message);
	}

}
