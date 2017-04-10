package com.dianping.pigeon.remoting.invoker.client;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.util.HeartBeatSupport;
import com.dianping.pigeon.remoting.common.channel.Channel;
import com.dianping.pigeon.remoting.common.codec.SerializerType;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.concurrent.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

/**
 * @author qi.yin
 *         2016/09/21  下午7:58.
 */
public class HeartbeatTask implements Runnable {

    private static final Logger logger = LoggerLoader.getLogger(HeartbeatTask.class);

    private static AtomicLong heartBeatSeq = new AtomicLong();

    private static final Monitor monitor = MonitorLoader.getMonitor();

    private final Client client;
    private final ClientConfig clientConfig;
    private final HeartbeatStats heartbeatStats = new HeartbeatStats();

    public HeartbeatTask(ClientConfig clientConfig, Client client) {
        this.clientConfig = clientConfig;
        this.client = client;
    }

    @Override
    public void run() {
        if (isSend(client.getAddress())) {
            boolean allFailed = heartbeatChannel();
            notifyClientStateChanged(allFailed);
        }
    }

    private boolean heartbeatChannel() {
        List<Channel> channels = this.client.getChannels();

        boolean allFailed = true;

        if (channels != null) {

            for (int index = 0; index < channels.size(); index++) {

                Channel channel = channels.get(index);
                if (channel != null) {
                    try {
                        if (channel.isAvaliable()) {
                            boolean isSuccess = sendHeartBeat(client, channel);

                            if (isSuccess) {
                                allFailed = false;
                            }
                        }

                    } catch (Exception e) {
                        logger.warn("[run] heartbeat failed. Channel" + channel, e);
                    }
                }
            }

        }
        return allFailed;
    }

    private void notifyClientStateChanged(boolean allFailed) {
        if (allFailed) {
            heartbeatStats.incFailedCount();
        } else {
            heartbeatStats.incSuccessCount();
        }

        if (heartbeatStats.getFailedCount() >= clientConfig.getDeadThreshold()) {
            if (clientConfig.isHeartbeatAutoPickOff()) {
                if (client.isActive()) {
                    client.setActive(false);
                    disConnectChannel(client);
                    logger.info("[notifyClientStateChanged]heartbeat" + client + ", inactive addresses:" + client.getAddress());
                    monitor.logEvent("PigeonCall.heartbeat", "Deactivate", client.getAddress());
                }
            }
            heartbeatStats.resetStats();
        } else if (heartbeatStats.getSuccessCount() >= clientConfig.getHealthThreshold()) {

            if (!client.isActive()) {
                client.setActive(true);
                logger.info("[notifyClientStateChanged]heartbeat" + client + ", active addresses:" + client.getAddress());
                monitor.logEvent("PigeonCall.heartbeat", "Activate", client.getAddress());
            }
            heartbeatStats.resetStats();
        }
    }

    private void disConnectChannel(Client client) {
        List<Channel> channels = client.getChannels();
        if (channels != null) {
            for (Channel channel : channels) {
                if (channel != null && channel.isAvaliable()) {
                    channel.disConnect();
                    logger.info("[disConnectChannel] close avaliable channel. address : " + channel.getRemoteAddressString());
                }
            }
        }

    }


    public boolean sendHeartBeat(Client client, Channel channel) {
        boolean isSuccess = true;
        String address = channel.getRemoteAddressString();

        InvocationRequest request = createHeartRequest(address);

        try {
            InvocationResponse response = null;
            CallbackFuture future = new CallbackFuture();

            InvokerUtils.sendRequest(client, channel, request, future);

            response = future.getResponse(clientConfig.getHeartbeatTimeout());

            if (response != null && !(response.getReturn() instanceof Exception)) {
                isSuccess = true;
            } else {
                logger.info("[heartbeat] send heartbeat to server[" + address + "] failed.");
                isSuccess = false;
            }
        } catch (Throwable e) {
            logger.info("[heartbeat] send heartbeat to server[" + address + "] failed", e);
            isSuccess = false;
        }

        return isSuccess;
    }

    private boolean isSend(String address) {
        boolean supported = true;
        byte heartBeatSupport = RegistryManager.getInstance().getServerHeartBeatSupportFromCache(address);

        switch (HeartBeatSupport.findByValue(heartBeatSupport)) {
            case NoSupport:
            case ScannerOnly:
                supported = false;
                break;

            case P2POnly:
            case BothSupport:
            default:
                supported = true;
                break;
        }

        return supported;
    }

    private boolean supported(String address) {
        boolean supported = false;
        try {
            supported = RegistryManager.getInstance().isSupportNewProtocol(address);
        } catch (Throwable t) {
            supported = Constants.isSupportedNewProtocol();
            logger.warn("get protocol support failed, set support to: " + supported);
        }

        return supported;
    }

    private InvocationRequest createHeartRequest0(String address) {
        InvocationRequest request = InvocationUtils.newRequest(Constants.HEART_TASK_SERVICE + address, Constants.HEART_TASK_METHOD,
                null, SerializerType.HESSIAN.getCode(), Constants.MESSAGE_TYPE_HEART, clientConfig.getHeartbeatTimeout(), null);
        request.setSequence(generateHeartSeq());
        request.setCreateMillisTime(System.currentTimeMillis());
        request.setCallType(Constants.CALLTYPE_REPLY);
        return request;
    }

    private InvocationRequest createHeartRequest_(String address) {
        InvocationRequest request = new GenericRequest(Constants.HEART_TASK_SERVICE + address, Constants.HEART_TASK_METHOD,
                null, SerializerType.THRIFT.getCode(), Constants.MESSAGE_TYPE_HEART, clientConfig.getHeartbeatTimeout());
        request.setSequence(generateHeartSeq());
        request.setCreateMillisTime(System.currentTimeMillis());
        request.setCallType(Constants.CALLTYPE_REPLY);
        return request;
    }

    private InvocationRequest createHeartRequest(String address) {
        if (supported(address)) {
            return createHeartRequest_(address);
        } else {
            return createHeartRequest0(address);
        }
    }

    private long generateHeartSeq() {
        return heartBeatSeq.getAndIncrement();
    }


    class HeartbeatStats {

        private AtomicLong failedCount;

        private AtomicLong successCount;

        public HeartbeatStats() {
            this(0L, 0L);
        }

        public HeartbeatStats(long failedCount, long successCount) {
            this.failedCount = new AtomicLong(failedCount);
            this.successCount = new AtomicLong(successCount);
        }

        public long getFailedCount() {
            return failedCount.get();
        }

        public void incFailedCount() {
            resetSuccessCount();
            failedCount.incrementAndGet();
        }

        public void resetFailedCount() {
            failedCount.set(0L);
        }

        public long getSuccessCount() {
            return successCount.get();
        }

        public void incSuccessCount() {
            resetFailedCount();
            successCount.incrementAndGet();
        }

        public void resetSuccessCount() {
            successCount.set(0L);
        }

        public void resetStats() {
            resetSuccessCount();
            resetFailedCount();
        }

    }

}
