/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public final class ExtensionLoader {

	private static Map<Class<?>, Object> extensionMap = new ConcurrentHashMap<Class<?>, Object>();

	private static Map<Class<?>, List<?>> extensionListMap = new ConcurrentHashMap<Class<?>, List<?>>();

	private ExtensionLoader() {
	}

	public static <T> T getExtension(Class<T> clazz) {
		T extension = (T) extensionMap.get(clazz);
		if (extension == null) {
			extension = newExtension(clazz);
			if (extension != null) {
				extensionMap.put(clazz, extension);
			}
		}
		return extension;
	}

	/**
	 * 第一次调用ExtensionLoader.getExtensionList(Server.class)时，
	 * 会创建配置了SPI的所有的Server实例，
	 * Pigeon中默认会创建JettyHttpServer和NettyServer这两个对象。
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> getExtensionList(Class<T> clazz) {
		List<T> extensions = (List<T>) extensionListMap.get(clazz);
		if (extensions == null) {
			extensions = newExtensionList(clazz);
			if (!extensions.isEmpty()) {
				extensionListMap.put(clazz, extensions);
			}
		}
		return extensions;
	}

	public static <T> T newExtension(Class<T> clazz) {
		ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
		for (T service : serviceLoader) {
			return service;
		}
		return null;
	}



	public static <T> List<T> newExtensionList(Class<T> clazz) {
        // TODO: 2019/1/8
		ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
		List<T> extensions = new ArrayList<T>();
		for (T service : serviceLoader) {
			extensions.add(service);
		}
		return extensions;
	}
}
