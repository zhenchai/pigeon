package com.dianping.pigeon.remoting.http.provider;

import java.util.concurrent.TimeUnit;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.callback.CallFuture;
import com.dianping.pigeon.remoting.invoker.callback.Callback;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

/**
 * Created by chenchongze on 16/1/13.
 */
public class HttpCallbackFuture implements Callback, CallFuture {

    protected final Logger logger = LoggerLoader.getLogger(this.getClass());

    protected InvocationRequest request;
    protected ProviderContext invocationContext;
    protected InvocationResponse response;
    private boolean done = false;

    public HttpCallbackFuture(InvocationRequest request, ProviderContext invocationContext) {
        this.request = request;
        this.invocationContext = invocationContext;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.done = true;
            this.notifyAll();
        }
    }

    @Override
    public InvocationResponse get(long timeoutMillis) throws InterruptedException {
        synchronized (this) {
            long start = request.getCreateMillisTime();
            while (!this.done) {
                long timeoutMillis_ = timeoutMillis - (System.currentTimeMillis() - start);
                if (timeoutMillis_ <= 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("request timeout, current time:").append(System.currentTimeMillis())
                            .append("\r\nrequest:").append(request);
                    RequestTimeoutException e = new RequestTimeoutException("invoke timeout");
                    invocationContext.getChannel().write(ProviderUtils.createFailResponse(request, e));
                    logger.error(sb.toString(), e);
                } else {
                    this.wait(timeoutMillis_);
                }
            }
        }
        return null;
    }

    @Override
    public InvocationResponse get() throws InterruptedException {
        return get(Long.MAX_VALUE);
    }

    @Override
    public InvocationResponse get(long timeout, TimeUnit unit) throws InterruptedException {
        return get(unit.toMillis(timeout));
    }

    @Override
    public void callback(InvocationResponse response) {
        this.response = response;
    }

    @Override
    public void setRequest(InvocationRequest request) {
        this.request = request;
    }

    @Override
    public boolean isDone() {
        return done;
    }



    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void setClient(Client client) {

    }

    @Override
    public Client getClient() {
        return null;
    }


}
