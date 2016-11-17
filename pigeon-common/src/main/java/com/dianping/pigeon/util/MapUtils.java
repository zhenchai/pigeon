package com.dianping.pigeon.util;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qi.yin
 *         2016/11/08  下午7:36.
 */
public class MapUtils {


    public static <K, V> V getOrCreate(Map<K, V> map, K key, ObjectFactory<? extends V> objectFactory) {

        V ret = map.get(key);

        if (ret != null) {
            return ret;
        }

        synchronized (map) {
            ret = (V) map.get(key);
            if (ret == null) {
                try {
                    ret = objectFactory.createObject();
                } catch (Exception e) {
                    throw new IllegalStateException("error create object from factory:" + objectFactory, e);
                }
                map.put(key, ret);
            }
        }
        return ret;

    }

    public static <K, V, T extends V> V getOrCreate(Map<K, V> map, K key, Class<T> clazz) {

        return getOrCreate(map, key, new ReflectObjectFactory<T>(clazz));
    }

    public static <K, V> V getOrCreate(ConcurrentMap<K, V> map, K key, ObjectFactory<? extends V> objectFactory) {

        V ret = map.get(key);

        if (ret != null) {
            return ret;
        } else {
            ret = objectFactory.createObject();

            V last = map.putIfAbsent(key, ret);

            if (last == null) {
                return ret;
            } else {
                return last;
            }
        }

    }


    public static <K, V, T extends V> V getOrCreate(ConcurrentMap<K, V> map, K key, Class<T> clazz) {

        return getOrCreate(map, key, new ReflectObjectFactory<T>(clazz));
    }


    public static class ReflectObjectFactory<T> implements ObjectFactory<T> {

        private Class<T> clazz;

        public ReflectObjectFactory(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T createObject() {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("error create object for " + clazz, e);
            }
        }

        @Override
        public Class<T> getObjectClass() {
            return clazz;
        }

        @Override
        public String toString() {
            return "object class:" + clazz;
        }

    }
}
