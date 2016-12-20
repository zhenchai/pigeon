package com.dianping.pigeon.remoting.test;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.PoolConfig;
import com.dianping.pigeon.remoting.provider.process.threadpool.DynamicThreadPoolFactory;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 16/10/21.
 */
public class PoolTest {

    JacksonSerializer serializer = new JacksonSerializer();
    private String poolConfig = "[\n" +
            "\t{\n" +
            "\t\t\"poolName\" : \"pool1\",\n" +
            "\t\t\"corePoolSize\" : \"50\",\n" +
            "\t\t\"maxPoolSize\" : \"100\",\n" +
            "\t\t\"workQueueSize\" : \"100\"\n" +
            "\t},\n" +
            "\t{\n" +
            "\t\t\"poolName\" : \"pool2\",\n" +
            "\t\t\"corePoolSize\" : \"30\",\n" +
            "\t\t\"maxPoolSize\" : \"80\",\n" +
            "\t\t\"workQueueSize\" : \"80\"\n" +
            "\t}\n" +
            "]";
    private String serviceConfig = "{\n" +
            "\t\"http://service.dianping.com/arch/test/service/EchoService_1.0.0\" : \"pool2\"\n" +
            "}";
    private String methodConfig = "{\n" +
            "\t\"http://service.dianping.com/arch/test/service/EchoService_1.0.0#echo\" : \"pool1\",\n" +
            "\t\"http://service.dianping.com/arch/test/service/EchoService_1.0.0#statistics\" : \"pool2\"\n" +
            "}";
    private String json = "{\n" +
            "\t\"poolName\" : \"pool1\",\n" +
            //"\t\"corePoolSize\" : \"50\",\n" +
            //"\t\"maxPoolSize\" : \"100\",\n" +
            "\t\"workQueueSize\" : \"100\"\n" +
            "}";

    @Test
    public void test00() {
        System.out.println();
        String config = "core=100,max=200,queue=200";
        Map<String, Integer> map = Maps.newHashMap();

        for (String arg : config.split(",")) {
            String[] kv = arg.split("=");
            if (kv.length == 2) {
                map.put(kv[0], Integer.parseInt(kv[1]));
            }
        }

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    @Test
    public void test000() {
        ThreadPoolExecutor e = new ThreadPoolExecutor(1,1,-1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(40));
        String stats = String.format(
                "request pool size:%d(active:%d,core:%d,max:%d,largest:%d),task count:%d(completed:%d),queue size:%d,queue remaining:%d",
                e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.getQueue().size(), e.getQueue().remainingCapacity());
        System.out.println(stats);
        e.setCorePoolSize(5);
        e.setMaximumPoolSize(30);
        System.out.println(String.format(
                "request pool size:%d(active:%d,core:%d,max:%d,largest:%d),task count:%d(completed:%d),queue size:%d,queue remaining:%d",
                e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.getQueue().size(), e.getQueue().remainingCapacity()));
    }

    @Test
    public void testInterner() {
        ThreadPool threadPool = new DefaultThreadPool("test",500,500);

        for (int i = 0; i < 500; ++i) {
            threadPool.getExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    DynamicThreadPoolFactory.getThreadPool(new PoolConfig());
                }
            });
        }
    }
}
