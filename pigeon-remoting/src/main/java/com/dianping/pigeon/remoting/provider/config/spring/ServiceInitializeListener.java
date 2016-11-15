package com.dianping.pigeon.remoting.provider.config.spring;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.util.CollectionUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;

import java.util.Map;

public class ServiceInitializeListener implements ApplicationListener {

	private static final Logger logger = LoggerLoader.getLogger(ServiceInitializeListener.class);

	public void onApplicationEvent(ApplicationEvent event) {
		synchronized (ServiceInitializeListener.class) {
			if (ServicePublisher.isAutoPublish() && !isOnline()) {
				if (event instanceof ContextRefreshedEvent) {
					ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent) event;
					if (refreshEvent.getApplicationContext().getParent() == null) {
						logger.info("service initialized");
						try {
							ServiceFactory.online();
						} catch (RegistryException e) {
							logger.error("error with services online", e);
						}
					}
				}
			}
		}
	}

	private static boolean isOnline() {
		boolean isOnline = true;
		Map<String, Integer> weights = ServicePublisher.getServerWeight();
		if (!CollectionUtils.isEmpty(weights)) {
			for (Integer weight : weights.values()) {
				if (weight <= 0) {
					isOnline = false;
					break;
				}
			}
		} else {
			isOnline = false;
		}
		return isOnline;
	}
}
