package com.dianping.pigeon.remoting.invoker;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.client.ClientConfig;
import com.dianping.pigeon.remoting.invoker.client.HeartbeatTask;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.route.region.Region;
import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.threadpool.NamedThreadFactory;

public abstract class AbstractClient implements Client {

    protected final Logger logger = LoggerLoader.getLogger(getClass());

    /**
     * 地区
     */
    protected volatile Region region;

    protected volatile boolean isActive = true;

    protected final AtomicBoolean isClosed = new AtomicBoolean(true);

    private final ResponseProcessor responseProcessor;

    private ScheduledFuture<?> heatbeatTimer;

    protected ClientConfig clientConfig;

    private static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(
            4, new NamedThreadFactory("Pigeon-Client-HeartBeat-ThreadPool", true));

    public AbstractClient(ClientConfig clientConfig, ResponseProcessor responseProcessor) {
        this.clientConfig = clientConfig;
        this.responseProcessor = responseProcessor;
    }

    public void open() {
        if (isClosed.compareAndSet(true, false)) {
            doOpen();
            startHeatbeat();
        }
    }

    public abstract void doOpen();

    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            doClose();
            stopHeartbeat();
        }
    }

    public abstract void doClose();

    @Override
    public InvocationResponse write(InvocationRequest request) throws NetworkException {
        ServiceStatisticsHolder.flowIn(request, this.getAddress());
        try {
            return doWrite(request);
        } catch (NetworkException e) {
            ServiceStatisticsHolder.flowOut(request, this.getAddress());
            throw e;
        }
    }

    public abstract InvocationResponse doWrite(InvocationRequest request) throws NetworkException;


    @Override
    public void processResponse(InvocationResponse response) {
        this.responseProcessor.processResponse(response, this);
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public Region getRegion() {
        if (region == null) {
            region = RegionPolicyManager.INSTANCE.getRegion(getHost());
        }
        return region;
    }

    @Override
    public void clearRegion() {
        region = null;
    }

    private void startHeatbeat() {
        stopHeartbeat();

        if (clientConfig.isHeartbeated() && Constants.PROTOCOL_DEFAULT.equals(getProtocol())) {

            heatbeatTimer = scheduled.scheduleWithFixedDelay(
                    new HeartbeatTask(clientConfig, AbstractClient.this),
                    clientConfig.getHeartbeatInterval() * 4,
                    clientConfig.getHeartbeatInterval(),
                    TimeUnit.MILLISECONDS);
        }
    }

    private void stopHeartbeat() {
        if (heatbeatTimer != null && !heatbeatTimer.isCancelled()) {
            try {
                heatbeatTimer.cancel(true);
                scheduled.purge();
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        }
        heatbeatTimer = null;
    }
}
