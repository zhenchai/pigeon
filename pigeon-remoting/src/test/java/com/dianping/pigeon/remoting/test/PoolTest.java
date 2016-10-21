package com.dianping.pigeon.remoting.test;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.provider.config.spring.PoolBean;
import org.junit.Test;

/**
 * Created by chenchongze on 16/10/21.
 */
public class PoolTest {

    @Test
    public void test1() {
        JacksonSerializer serializer = new JacksonSerializer();
        String json = "{\n" +
                "\t\"poolName\" : \"pool1\",\n" +
                "\t\"corePoolSize\" : \"50\",\n" +
                "\t\"maxPoolSize\" : \"100\",\n" +
                "\t\"workQueueSize\" : \"100\"\n" +
                "}";
        try {
            PoolBean poolBean = (PoolBean)serializer.toObject(PoolBean.class, json);
            System.out.println(poolBean);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
