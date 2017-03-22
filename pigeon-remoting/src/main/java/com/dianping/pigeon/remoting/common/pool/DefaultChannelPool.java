package com.dianping.pigeon.remoting.common.pool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.channel.Channel;
import com.dianping.pigeon.remoting.common.channel.ChannelFactory;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;
import com.dianping.pigeon.util.AtomicPositiveInteger;

/**
 * @author qi.yin 2016/09/23 上午10:52.
 */
public class DefaultChannelPool<C extends Channel> implements ChannelPool<C> {

    private static final Logger logger = LoggerLoader.getLogger(DefaultChannelPool.class);

    private final List<C> pooledChannels = new ArrayList<C>();

    private AtomicInteger size = new AtomicInteger();

    private AtomicPositiveInteger selectedIndex = new AtomicPositiveInteger(0);

    private AtomicBoolean isClosed = new AtomicBoolean(true);

    private PoolProperties properties;

    private ChannelFactory<C> channelFactory;

    private ScheduledFuture<?> scheduledFuture = null;

    private static ExecutorService reconnectExecutor = Executors.newFixedThreadPool(4,
            new DefaultThreadFactory("Pigeon-ChannelPool-Reconnect-Pool"));

    private static ScheduledThreadPoolExecutor checkScheduler = new ScheduledThreadPoolExecutor(2,
            new DefaultThreadFactory("Pigeon-ChannelPool-Check-Pool"));

    private final static Object PRESENT = new Object();

    private static final ConcurrentMap<Channel, Object> reconnectChannels = new ConcurrentHashMap<Channel, Object>();

    public DefaultChannelPool(ChannelFactory channelFactory) throws ChannelPoolException {
        this(new PoolProperties(), channelFactory);
    }

    public DefaultChannelPool(PoolProperties properties, ChannelFactory channelFactory) throws ChannelPoolException {
        if (isClosed.compareAndSet(true, false)) {
            this.properties = properties;
            this.channelFactory = channelFactory;
            init(properties);
        }
    }

    private void init(PoolProperties properties) throws ChannelPoolException {
        if (properties.getMaxActive() < 1) {
            logger.info(
                    "[init] maxActive is smaller than 1, setting maxActive to " + PoolProperties.DEFAULT_MAX_ACTIVE);
            properties.setMaxActive(PoolProperties.DEFAULT_MAX_ACTIVE);
        }
        if (properties.getInitialSize() > properties.getMaxActive()) {
            logger.info(
                    "[init] initialSize is larger than maxActive, setting initialSize to" + properties.getMaxActive());
            properties.setInitialSize(properties.getMaxActive());
        }

        if (properties.getNormalSize() > properties.getMaxActive()) {
            logger.info(
                    "[init] normalSize is larger than maxActive, setting normalSize to" + properties.getMaxActive());
            properties.setNormalSize(properties.getMaxActive());
        }

        if (properties.getNormalSize() < properties.getInitialSize()) {
            logger.info("[init] normalSize is smaller than initialSize, setting normalSize to"
                    + properties.getInitialSize());
            properties.setNormalSize(properties.getInitialSize());
        }

        try {

            for (int i = 0; i < properties.getInitialSize(); i++) {
                selectChannel();
            }

        } catch (ChannelPoolException e) {
            logger.info("[init] unable to create initial connections of pool.", e);
        }

        initCheckScheduler();
    }

    private void initCheckScheduler() {
        int interval = properties.getTimeBetweenCheckerMillis();

        scheduledFuture = checkScheduler.scheduleWithFixedDelay(new CheckChannelTask(this), interval, interval,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public int getSize() {
        return pooledChannels.size();
    }

    @Override
    public boolean isAvaliable() {
        if (isClosed()) {
            return false;
        }

        for (int index = 0; index < pooledChannels.size(); index++) {

            C channel = pooledChannels.get(index);

            if (channel != null && channel.isAvaliable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public C selectChannel() throws ChannelPoolException {
        if (isClosed()) {
            throw new ChannelPoolException("Channel pool is closed.");
        }

        long now = System.nanoTime();

        long maxWait = (properties.getMaxWait() < 0) ? Long.MAX_VALUE : properties.getMaxWait();

        do {
            // create
            if (size.get() < properties.getNormalSize()) {
                if (size.incrementAndGet() > properties.getNormalSize()) {
                    size.decrementAndGet();
                } else {
                    return createChannel();
                }
            }

            // random
            if (!pooledChannels.isEmpty()) {
                int selected = selectedIndex.getAndIncrement() % pooledChannels.size();
                C pooledChannel = pooledChannels.get(selected);

                if (pooledChannel != null) {
                    if (!pooledChannel.isAvaliable()) {
                        reconnectChannel(pooledChannel, this);
                    } else {
                        return pooledChannel;
                    }
                }
            }

            // timeout
            if (((System.nanoTime() - now) / 1000000) >= maxWait) {
                throw new ChannelPoolException(
                        "TimeOut:pool empty. Unable to fetch a channel, none avaliable in use." + getChannelPoolDesc());
            }

        } while (true);

    }

    protected C createChannel() {
        C channel = null;

        try {
            channel = channelFactory.createChannel();
        } finally {
            if (channel != null) {
                synchronized (pooledChannels) {
                    pooledChannels.add(channel);
                }
            } else {
                size.decrementAndGet();
            }
        }

        return channel;
    }

    protected static void reconnectChannel(Channel channel, ChannelPool channelPool) {
        if (reconnectChannels.putIfAbsent(channel, PRESENT) == null) {
            reconnectExecutor.submit(new ReconnectChannelTask(channel, channelPool));
        }
    }

    @Override
    public List<C> getChannels() {
        return pooledChannels;
    }

    @Override
    public PoolProperties getPoolProperties() {
        return properties;
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {

            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
                checkScheduler.purge();
            }

            scheduledFuture = null;

            for (int index = 0; index < pooledChannels.size(); index++) {

                C pooledChannel = pooledChannels.get(index);

                if (pooledChannel != null) {
                    if (pooledChannel.isAvaliable()) {
                        pooledChannel.disConnect();
                    } else {
                        if (reconnectChannels.containsKey(pooledChannel)) {
                            reconnectChannels.remove(pooledChannel);
                        }
                    }
                }
            }


        }
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    protected String getChannelPoolDesc() {
        return "ChannelPool[poolSize=" + pooledChannels.size() + "]";
    }

    static class ReconnectChannelTask implements Runnable {

        private WeakReference<Channel> channelRef;
        private WeakReference<ChannelPool> poolRef;

        public ReconnectChannelTask(Channel channel, ChannelPool pool) {
            this.channelRef = new WeakReference<Channel>(channel);
            this.poolRef = new WeakReference<ChannelPool>(pool);
        }

        @Override
        public void run() {
            ChannelPool channelPool = poolRef.get();
            Channel channel = channelRef.get();

            try {
                if (channelPool != null && !channelPool.isClosed()) {

                    if (channel != null && !channel.isAvaliable()) {
                        try {
                            channel.connect();
                        } catch (NetworkException e) {
                            logger.info("[run] pooledChannel connect failed. remoteAddress : " + channel.getRemoteAddressString());
                        }

                    }
                }
            } finally {
                reconnectChannels.remove(channel);
            }

        }
    }

    static class CheckChannelTask implements Runnable {

        private WeakReference<ChannelPool> poolRef;

        public CheckChannelTask(ChannelPool channelPool) {
            this.poolRef = new WeakReference<ChannelPool>(channelPool);
        }

        @Override
        public void run() {
            try {
                ChannelPool channelPool = poolRef.get();

                if (channelPool != null && !channelPool.isClosed()) {

                    List<Channel> channels = channelPool.getChannels();

                    for (int index = 0; index < channels.size(); index++) {
                        Channel channel = channels.get(index);

                        if (!channel.isAvaliable()) {
                            reconnectChannel(channel, channelPool);
                        }
                    }

                }
            } catch (Throwable t) {
                logger.info("[run] pooledChannel check failed.", t);
            }

        }
    }
}
