package com.dianping.pigeon.remoting.invoker.domain;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.concurrent.Callback;


/**
 * netty，请求，响应的Bean，通过 Sequence 关联request、response
 */
public class RemoteInvocationBean {

	public InvocationRequest request;
	public Callback callback;

}
