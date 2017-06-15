/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.test;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.route.balance.AbstractLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.region.Region;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.threadpool.DynamicThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.LangUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class TestWeightedAutoawareLoadBalance extends AbstractLoadBalance {

    private static final Logger logger = LoggerLoader.getLogger(TestWeightedAutoawareLoadBalance.class);
    public static final String NAME = "weightedAutoaware";
    public static final LoadBalance instance = new TestWeightedAutoawareLoadBalance();
    private static int defaultFactor = ConfigManagerLoader.getConfigManager().getIntValue(
            "pigeon.loadbalance.defaultFactor", 100);
    private static Random random = new Random();

    @Override
    public Client doSelect(List<Client> clients, InvokerConfig<?> invokerConfig, InvocationRequest request,
                           int[] weights) {
        assert (clients != null && clients.size() >= 1);
        if (clients.size() == 1) {
            return clients.get(0);
        }
        float minCapacity = Float.MAX_VALUE;
        int clientSize = clients.size();
        Client[] candidates = new Client[clientSize];
        int candidateIdx = 0;
        for (int i = 0; i < clientSize; i++) {
            Client client = clients.get(i);
            float capacity = 1;
            if (logger.isDebugEnabled()) {
                logger.info("capacity:" + LangUtils.toString(capacity, 4) + ", weight:" + weights[i] + " for address:"
                        + client.getAddress());
            }

            // 根据权重重新调整请求容量
            if (weights[i] > 0) {
                capacity /= weights[i];
            } else {
                capacity = Float.MAX_VALUE;
            }

            if (capacity < minCapacity) {
                minCapacity = capacity;
                candidateIdx = 0;
                candidates[candidateIdx++] = client;
            } else if (Math.abs(capacity - minCapacity) < 1e-6) {
                candidates[candidateIdx++] = client;
            }
        }
        Client client = candidateIdx == 1 ? candidates[0] : candidates[random.nextInt(candidateIdx)];
        if (logger.isDebugEnabled()) {
            logger.debug("select address:" + client.getAddress());
        }
        return client;
    }

    public static void main(String[] args) {
        final List<Client> clients = new ArrayList<>();
        final Map<String, AtomicLong> counts = new HashMap<>();

        for (int i = 0; i < 5; ++i) {
            String key = "ccz" + i;
            Client client = new TestClient(key);
            clients.add(client);
            counts.put(key, new AtomicLong(0));
        }

        final int[] weights = { 10000, 100, 100, 100, 100, 0};

        final TestWeightedAutoawareLoadBalance tw = new TestWeightedAutoawareLoadBalance();

        final ThreadPool pool = new DynamicThreadPool("test-lb-pool", 300, 300, Integer.MAX_VALUE);

        final CountDownLatch latch = new CountDownLatch(300);

        for (int i = 0; i < 300; i++) {
            try {
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < 300; j++) {
                            Client client = tw.doSelect(clients, null, null, weights);
                            counts.get(client.getAddress()).incrementAndGet();
                        }
                        latch.countDown();
                    }
                });
            } catch (Throwable t) {
                latch.countDown();
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, AtomicLong> entry : counts.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }

    }


    private static class TestClient implements Client {

        private final String address;

        public TestClient(String address) {
            this.address = address;
        }

        @Override
        public ConnectInfo getConnectInfo() {
            return null;
        }

        @Override
        public void open() {

        }

        @Override
        public void close() {

        }

        @Override
        public InvocationResponse write(InvocationRequest request) throws NetworkException {
            return null;
        }

        @Override
        public void processResponse(InvocationResponse response) {

        }

        @Override
        public void setActive(boolean active) {

        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public List getChannels() {
            return null;
        }

        @Override
        public String getHost() {
            return null;
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public Region getRegion() {
            return null;
        }

        @Override
        public void clearRegion() {

        }
    }
}
