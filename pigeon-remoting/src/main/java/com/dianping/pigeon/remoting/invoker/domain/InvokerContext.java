/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import com.dianping.pigeon.remoting.common.monitor.trace.InvokerMonitorData;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public interface InvokerContext<M extends InvokerMonitorData> extends InvocationContext<M> {

    InvokerConfig<?> getInvokerConfig();

    String getMethodName();

    Class<?>[] getParameterTypes();

    Object[] getArguments();

    Client getClient();

    void setClient(Client client);

    DegradeInfo getDegradeInfo();
}
