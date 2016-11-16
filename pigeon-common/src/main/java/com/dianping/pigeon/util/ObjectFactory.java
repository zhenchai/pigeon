package com.dianping.pigeon.util;

/**
 * @author qi.yin
 *         2016/11/08  下午7:55.
 */
public interface ObjectFactory<T> {

    T createObject();

    Class<T> getObjectClass();

}