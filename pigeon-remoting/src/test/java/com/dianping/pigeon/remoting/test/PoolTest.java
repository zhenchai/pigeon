package com.dianping.pigeon.remoting.test;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.PoolConfig;
import com.dianping.pigeon.remoting.provider.config.spring.PoolBean;
import com.dianping.pigeon.remoting.provider.process.threadpool.ThreadPoolFactory;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    // poolName --> poolBean
    Map<String, PoolBean> poolNameMapping = Maps.newConcurrentMap();
    // url --> poolName
    Map<String, String> servicePoolConfigMapping = Maps.newConcurrentMap();
    // url#api --> poolName
    Map<String, String> methodPoolConfigMapping = Maps.newConcurrentMap();

    @Test
    public void test2() {

        // pigeon.provider.pool.config
        try {
            PoolBean[] poolBeen = (PoolBean[])serializer.toObject(PoolBean[].class, poolConfig);
            Map<String, PoolBean> _poolNameMapping = Maps.newConcurrentMap();

            for (PoolBean poolBean : poolBeen) {
                if(poolBean.validate()) {
                    _poolNameMapping.put(poolBean.getPoolName(), poolBean);
                } else {//报异常,保持原状
                    throw new RuntimeException("pool config error! please check: " + poolBean);
                }
            }

            if(_poolNameMapping.size() != poolBeen.length) {//报异常,保持原状
                throw new RuntimeException("conflict pool name exists, please check!");
            } else {
                poolNameMapping = _poolNameMapping;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        //pigeon.provider.pool.service.config
        try {
            Map<String, String> _methodPoolConfigMapping = (Map) serializer.toObject(Map.class, serviceConfig);
            servicePoolConfigMapping = new ConcurrentHashMap<>(_methodPoolConfigMapping);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        //pigeon.provider.pool.method.config
        try {
            Map<String, String> _methodPoolConfigMapping = (Map) serializer.toObject(Map.class, methodConfig);
            methodPoolConfigMapping = new ConcurrentHashMap<>(_methodPoolConfigMapping);
        } catch (Throwable t) {
            t.printStackTrace();
        }


    }

    @Test
    public void test1() {

        PoolBean poolBean = null;
        try {
            poolBean = (PoolBean)serializer.toObject(PoolBean.class, json);
            poolBean.getRefreshedThreadPool();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test0() {
        PoolBean poolBean1 = new PoolBean();
        poolBean1.setPoolName("aaa");
        poolBean1.setCorePoolSize(1);
        poolBean1.setMaxPoolSize(2);
        poolBean1.setWorkQueueSize(3);
        PoolBean poolBean2 = new PoolBean();
        poolBean2.setPoolName("aaa");
        poolBean2.setCorePoolSize(1);
        poolBean2.setMaxPoolSize(2);
        poolBean2.setWorkQueueSize(3);

        poolBean1.getRefreshedThreadPool();
        poolBean2.getRefreshedThreadPool();

        Map<String, PoolBean> map1 = Maps.newConcurrentMap();
        Map<String, PoolBean> map2 = Maps.newConcurrentMap();
        map1.put("aaa", poolBean1);
        map2.put("aaa", poolBean2);

        for (String s : map1.keySet()) {
            PoolBean oldPoolBean = map1.get(s);
            PoolBean newPoolBean = map2.get(s);

            if(map1.get(s).equals(newPoolBean)) {
                map2.remove(s);
                oldPoolBean.setPoolName("nnn");
                map2.put(s, oldPoolBean);
            }
        }

        for (String s : map1.keySet()) {
            PoolBean poolBean = map1.get(s);
            System.out.println(poolBean);
        }

        for (String s : map2.keySet()) {
            PoolBean poolBean = map2.get(s);
            System.out.println(poolBean);
        }

    }

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
                    ThreadPoolFactory.getThreadPool(new PoolConfig("aaa"));
                }
            });
        }
    }
}
