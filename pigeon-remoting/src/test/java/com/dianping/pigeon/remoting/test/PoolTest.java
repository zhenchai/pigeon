package com.dianping.pigeon.remoting.test;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.spring.PoolBean;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                if(poolBean.checkNotNull()) {
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
            poolBean.getThreadPool();
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

        poolBean1.getThreadPool();
        poolBean2.getThreadPool();

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
}
