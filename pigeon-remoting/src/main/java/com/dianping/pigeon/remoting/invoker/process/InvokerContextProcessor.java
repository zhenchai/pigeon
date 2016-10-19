/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2014 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public interface InvokerContextProcessor {

	public void preInvoke(final InvokerContext invokerContext);

	public void postInvoke(final InvocationResponse response);

}
