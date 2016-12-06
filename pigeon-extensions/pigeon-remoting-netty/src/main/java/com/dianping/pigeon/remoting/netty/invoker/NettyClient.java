/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker;

import java.util.List;

import com.dianping.pigeon.remoting.invoker.client.ClientConfig;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.dianping.pigeon.remoting.common.channel.ChannelFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedRequest;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.pool.ChannelPool;
import com.dianping.pigeon.remoting.common.pool.ChannelPoolException;
import com.dianping.pigeon.remoting.common.pool.DefaultChannelPool;
import com.dianping.pigeon.remoting.common.pool.PoolProperties;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.AbstractClient;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.netty.channel.NettyChannel;
import com.dianping.pigeon.remoting.netty.channel.NettyChannelFactory;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import com.dianping.pigeon.util.NetUtils;

public class NettyClient extends AbstractClient {

    private String protocol = Constants.PROTOCOL_DEFAULT;

    private ConnectInfo connectInfo;

    private String remoteHost;

    private int remotePort;

    private int timeout;

    private ClientBootstrap bootstrap;

    private String remoteAddressString;

    private ChannelPool<NettyChannel> channelPool;

    private PoolProperties poolProperties;

    private org.jboss.netty.channel.ChannelFactory channelFactory;

    public NettyClient(ClientConfig clientConfig,
                       org.jboss.netty.channel.ChannelFactory channelFactory,
                       ConnectInfo connectInfo,
                       ResponseProcessor responseProcessor) {

        super(clientConfig, responseProcessor);
        this.channelFactory = channelFactory;
        this.connectInfo = connectInfo;
        this.remoteHost = connectInfo.getHost();
        this.remotePort = connectInfo.getPort();
        this.remoteAddressString = NetUtils.toAddress(remoteHost, remotePort);
        this.timeout = clientConfig.getConnectTimeout();
        poolProperties = new PoolProperties(
                clientConfig.getInitialSize(),
                clientConfig.getNormalSize(),
                clientConfig.getMaxActive(),
                clientConfig.getMaxWait(),
                clientConfig.getTimeBetweenCheckerMillis());
    }

    @Override
    public void doOpen() {
        try {
            initBootstrap();

            initChannelPool();
            logger.info("[open] client opened. remoteAddress: " + remoteAddressString);
        } catch (Exception e) {
            logger.info("[open] client open failed. remoteAddress: " + remoteAddressString);
        }
    }

    private void initBootstrap() {
        this.bootstrap = new ClientBootstrap(channelFactory);
        this.bootstrap.setPipelineFactory(new NettyClientPipelineFactory(this));
        this.bootstrap.setOption("tcpNoDelay", true);
        this.bootstrap.setOption("keepAlive", true);
        this.bootstrap.setOption("reuseAddress", true);
        this.bootstrap.setOption("connectTimeoutMillis", timeout);
        this.bootstrap.setOption("writeBufferHighWaterMark", clientConfig.getHighWaterMark());
        this.bootstrap.setOption("writeBufferLowWaterMark", clientConfig.getLowWaterMark());
    }

    private void initChannelPool() throws ChannelPoolException {
        channelPool = new DefaultChannelPool<NettyChannel>(poolProperties, createChannelFactory());
    }

    public ClientBootstrap getBootstrap() {
        return bootstrap;
    }

    public ChannelFactory createChannelFactory() {
        return new NettyChannelFactory(this);
    }


    @Override
    public InvocationResponse doWrite(InvocationRequest request) throws NetworkException {
        NettyChannel channel = null;

        try {

            channel = channelPool.selectChannel();

            ChannelFuture future = channel.write0(request);

            afterWrite(request, channel);

            if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE
                    || request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
                future.addListener(new MessageWriteListener(request, channel));
            }

        } catch (Exception e) {
            throw new NetworkException("[doRequest] remote call failed:" + request, e);
        }
        return null;
    }

    private void afterWrite(InvocationRequest request, NettyChannel channel) {
        if (request instanceof UnifiedRequest) {

            UnifiedRequest _request = (UnifiedRequest) request;

            _request.setClientIp(channel.getLocalAddress().getAddress().getHostAddress());
        }
    }

    @Override
    public void doClose() {
        try {
            channelPool.close();
            logger.info("[close] client closed. remoteAddress: " + remoteAddressString);
        } catch (Exception e) {
            logger.info("[close] client close failed. remoteAddress: " + remoteAddressString);
        }
    }

    @Override
    public List<NettyChannel> getChannels() {
        return channelPool == null ? null : channelPool.getChannels();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && poolActive();
    }

    private boolean poolActive() {
        return channelPool != null && channelPool.isAvaliable();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getHost() {
        return remoteHost;
    }

    @Override
    public int getPort() {
        return remotePort;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public String getAddress() {
        return remoteAddressString;
    }

    @Override
    public ConnectInfo getConnectInfo() {
        return connectInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NettyClient that = (NettyClient) o;

        if (remotePort != that.remotePort)
            return false;
        return !(remoteHost != null ? !remoteHost.equals(that.remoteHost) : that.remoteHost != null);

    }

    @Override
    public int hashCode() {
        int result = remoteHost != null ? remoteHost.hashCode() : 0;
        result = 31 * result + remotePort;
        return result;
    }

    @Override
    public String toString() {
        return "NettyClient[" + this.getAddress() + ", closed:" + isClosed() + ", active:" + isActive() + ", pool.Avaliable:" + poolActive() + "]";
    }

    public class MessageWriteListener implements ChannelFutureListener {

        private InvocationRequest request;

        private NettyChannel channel;

        public MessageWriteListener(InvocationRequest request, NettyChannel channel) {
            this.request = request;
            this.channel = channel;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                return;
            }

            InvocationResponse response = ProviderUtils.createFailResponse(request, future.getCause());
            processResponse(response);
        }
    }
}