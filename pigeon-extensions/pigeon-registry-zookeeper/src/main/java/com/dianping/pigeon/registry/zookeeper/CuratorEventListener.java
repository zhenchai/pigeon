package com.dianping.pigeon.registry.zookeeper;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.config.RegistryConfig;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.*;
import com.dianping.pigeon.registry.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuratorEventListener implements CuratorListener {

	private static Logger logger = LoggerLoader.getLogger(CuratorEventListener.class);

	private static final int ADDRESS = 1;
	private static final int WEIGHT = 2;
	private static final int APP = 3;
	private static final int VERSION = 4;
	private static final int PROTOCOL = 5;
	private static final int HOST_CONFIG= 6;

	private CuratorClient client;

	public CuratorEventListener(CuratorClient client) {
		this.client = client;
	}

	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent curatorEvent) throws Exception {
		WatchedEvent event = (curatorEvent == null ? null : curatorEvent.getWatchedEvent());

		if (event == null
				|| (event.getType() != EventType.NodeCreated && event.getType() != EventType.NodeDataChanged
						&& event.getType() != EventType.NodeDeleted && event.getType() != EventType.NodeChildrenChanged)) {
			return;
		}

		if (logger.isInfoEnabled())
			logEvent(event);

		try {
			PathInfo pathInfo = parsePath(event.getPath());
			if (pathInfo == null) {
				logger.warn("Failed to parse path " + event.getPath());
				return;
			}

			if (pathInfo.type == ADDRESS) {
				addressChanged(pathInfo);
			} else if (pathInfo.type == WEIGHT) {
				weightChanged(pathInfo);
			} else if (pathInfo.type == APP) {
				appChanged(pathInfo);
			} else if (pathInfo.type == VERSION) {
				versionChanged(pathInfo);
			} else if (pathInfo.type == PROTOCOL) {
				protocolChanged(pathInfo);
			} else if (pathInfo.type == HOST_CONFIG) {
				registryConfigChanged(pathInfo);
			}
		} catch (Throwable e) {
			logger.info("Error in ZookeeperWatcher.process()", e);
			return;
		}
	}

	private void logEvent(WatchedEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append("zookeeper event received, type: ").append(event.getType()).append(", path: ")
				.append(event.getPath());
		logger.info(sb);
	}

	/*
	 * 1. Get newest value from ZK and watch again 2. Determine if changed
	 * against cache 3. notify if changed 4. pay attention to group fallback
	 * notification
	 */
	private void addressChanged(PathInfo pathInfo) throws Exception {
		if (shouldNotify(pathInfo)) {
			String hosts = client.get(pathInfo.path);
			logger.info("Service address changed, path " + pathInfo.path + " value " + hosts);
			List<String[]> hostDetail = Utils.getServiceIpPortList(hosts);
			RegistryNotifyListenerLoader.getRegistryNotifyListener()
					.onServiceHostChange(pathInfo.serviceName, hostDetail, Constants.REGISTRY_CURATOR_NAME);
		}
		// Watch again
		client.watch(pathInfo.path);
	}

	private boolean shouldNotify(PathInfo pathInfo) throws Exception {
		String serviceName = pathInfo.serviceName;
		String currentGroup = RegistryManager.getInstance().getGroup(serviceName);
		currentGroup = Utils.normalizeGroup(currentGroup);
		if (currentGroup.equals(pathInfo.group))
			return true;
		if (StringUtils.isEmpty(currentGroup) && !StringUtils.isEmpty(pathInfo.group)) // 当前无group配置, 通知来自group
			return false;
		if (!StringUtils.isEmpty(currentGroup) && StringUtils.isEmpty(pathInfo.group)
				&& RegistryManager.fallbackDefaultGroup) { // group中 && 默认group改变的通知 && 可fallback
			String servicePath = Utils.getServicePath(pathInfo.serviceName, currentGroup);
			if (!client.exists(servicePath)) { // group地址为空或被删除,fallback
				return true;
			}
			String addr = client.get(servicePath); // group地址不为空,取出group的value
			if (!Utils.isValidAddress(addr)) { // group地址的value有效
				return true;
			}
		}
		return false;
	}

	private void weightChanged(PathInfo pathInfo) throws RegistryException {
		try {
			String newValue = client.get(pathInfo.path);
			logger.info("service weight changed, path " + pathInfo.path + " value " + newValue);
			int weight = newValue == null ? 0 : Integer.parseInt(newValue);
			RegistryNotifyListenerLoader.getRegistryNotifyListener()
					.onHostWeightChange(pathInfo.server, weight, Constants.REGISTRY_CURATOR_NAME);
			client.watch(pathInfo.path);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	private void appChanged(PathInfo pathInfo) throws RegistryException {
		try {
			String app = client.get(pathInfo.path);
			logger.info("app changed, path " + pathInfo.path + " value " + app);
			RegistryNotifyListenerLoader.getRegistryNotifyListener()
					.serverAppChanged(pathInfo.server, app, Constants.REGISTRY_CURATOR_NAME);
			client.watch(pathInfo.path);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	private void versionChanged(PathInfo pathInfo) throws RegistryException {
		try {
			String version = client.get(pathInfo.path);
			logger.info("version changed, path " + pathInfo.path + " value " + version);
			RegistryNotifyListenerLoader.getRegistryNotifyListener()
					.serverVersionChanged(pathInfo.server, version, Constants.REGISTRY_CURATOR_NAME);
			client.watch(pathInfo.path);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	private void protocolChanged(PathInfo pathInfo) throws Exception {
		try {
			String info = "{}";
			Map<String, Boolean> infoMap = new HashMap<>();
			try {
				info = client.get(pathInfo.path);
				infoMap = Utils.getProtocolInfoMap(info);
			} catch (KeeperException.NoNodeException e) {
				logger.info("failed to get protocol info: " + e.toString());
			}
			logger.info("protocol changed, path " + pathInfo.path + " value " + info);
			RegistryNotifyListenerLoader.getRegistryNotifyListener()
					.serverProtocolChanged(pathInfo.server, infoMap, Constants.REGISTRY_CURATOR_NAME);
		} catch (Throwable e) {
			throw new RegistryException(e);
		} finally {
			client.watch(pathInfo.path);
		}
	}

	private void registryConfigChanged(PathInfo pathInfo) throws Exception {
		try {
			String info = client.get(pathInfo.path);
			RegistryConfig registryConfig = Utils.getRegistryConfig(info);
			logger.info("registry config changed, path " + pathInfo.path + " value " + info);
			RegistryManager.getInstance().registryConfigChanged(pathInfo.server, registryConfig);
		} catch (Throwable e) {
			throw new RegistryException(e);
		} finally {
			client.watch(pathInfo.path);
		}
	}

	public PathInfo parsePath(String path) {
		if (path == null)
			return null;

		PathInfo pathInfo = null;
		if (path.startsWith(Constants.SERVICE_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = ADDRESS;
			pathInfo.serviceName = path.substring(Constants.SERVICE_PATH.length() + 1);
			int idx = pathInfo.serviceName.indexOf(Constants.PATH_SEPARATOR);
			if (idx != -1) {
				pathInfo.group = pathInfo.serviceName.substring(idx + 1);
				pathInfo.serviceName = pathInfo.serviceName.substring(0, idx);
			}
			pathInfo.serviceName = Utils.unescapeServiceName(pathInfo.serviceName);
			pathInfo.group = Utils.normalizeGroup(pathInfo.group);
		} else if (path.startsWith(Constants.WEIGHT_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = WEIGHT;
			pathInfo.server = path.substring(Constants.WEIGHT_PATH.length() + 1);
		} else if (path.startsWith(Constants.APP_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = APP;
			pathInfo.server = path.substring(Constants.APP_PATH.length() + 1);
		} else if (path.startsWith(Constants.VERSION_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = VERSION;
			pathInfo.server = path.substring(Constants.VERSION_PATH.length() + 1);
		} else if (path.startsWith(Constants.PROTOCOL_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = PROTOCOL;
			pathInfo.server = path.substring(Constants.PROTOCOL_PATH.length() + 1);
		} else if (path.startsWith(Constants.HOST_CONFIG_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = HOST_CONFIG;
			// use ip as server(without port)
			pathInfo.server = path.substring(Constants.HOST_CONFIG_PATH.length() + 1);
		}

		return pathInfo;
	}

	class PathInfo {
		String path;
		String serviceName;
		String group;
		String server;
		int type;

		PathInfo(String path) {
			this.path = path;
		}
	}

}
