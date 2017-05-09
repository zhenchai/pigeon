package com.dianping.pigeon.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

	public static boolean isEmpty(Collection collection) {
		return (collection == null || collection.isEmpty());
	}
	
	public static boolean isEmpty(Map map) {
		return (map == null || map.isEmpty());
	}

	/**
	 * 提取中括号中内容，忽略中括号中的中括号
	 * @param msg
	 * @return
	 */
	public static List<String> extractMessage(String msg) {

		List<String> list = new ArrayList<String>();
		int start = 0;
		int startFlag = 0;
		int endFlag = 0;
		for (int i = 0; i < msg.length(); i++) {
			if (msg.charAt(i) == '[') {
				startFlag++;
				if (startFlag == endFlag + 1) {
					start = i;
				}
			} else if (msg.charAt(i) == ']') {
				endFlag++;
				if (endFlag == startFlag) {
					list.add(msg.substring(start + 1, i));
				}
			}
		}
		return list;
	}
}
