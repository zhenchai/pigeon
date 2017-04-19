package com.dianping.pigeon.monitor;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;

import java.util.List;

public class MonitorLoader {

    private static final Logger logger = LoggerLoader.getLogger(MonitorLoader.class);

    private static List<Monitor> monitorList = ExtensionLoader.getExtensionList(Monitor.class);

    //管理监控组件，如cat和mtrace
	private static Monitor monitor = new CompositeMonitor(monitorList);

	static {
		monitor.init();
	}

	public static Monitor getMonitor() {
		return monitor;
	}
}
