package com.dianping.pigeon.remoting.provider.config.spring;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenchongze on 16/10/15.
 */
public class PoolBeanDefinitionParser implements BeanDefinitionParser {

    /** Default placeholder prefix: "${" */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
    /** Default placeholder suffix: "}" */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
    private final Class<?> beanClass;
    private final boolean required;
    private static AtomicInteger idCounter = new AtomicInteger();
    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    public PoolBeanDefinitionParser(Class<?> beanClass, boolean required) {
        this.beanClass = beanClass;
        this.required = required;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parse(element, parserContext, beanClass, required);
    }

    private BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass, boolean required) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setLazyInit(false);
        String id = element.getAttribute("id");
        if (StringUtils.isBlank(id)) {
            id = "pigeonPool_" + idCounter.incrementAndGet();
        }
        beanDefinition.setBeanClass(PoolBean.class);
        beanDefinition.setInitMethodName("init");

        MutablePropertyValues properties = beanDefinition.getPropertyValues();

        if (element.hasAttribute("poolName")) {
            properties.addPropertyValue("poolName", resolveReference(element, "poolName"));
        } else {
            properties.addPropertyValue("poolName", id);
        }

        if (element.hasAttribute("corePoolSize")) {
            properties.addPropertyValue("corePoolSize", resolveReference(element, "corePoolSize"));
            String value = element.getAttribute("corePoolSize");
            if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
                //
            }
        }
        if (element.hasAttribute("maxPoolSize")) {
            properties.addPropertyValue("maxPoolSize", resolveReference(element, "maxPoolSize"));
            String value = element.getAttribute("maxPoolSize");
            if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
                //
            }
        }
        if (element.hasAttribute("workQueueSize")) {
            properties.addPropertyValue("workQueueSize", resolveReference(element, "workQueueSize"));
            String value = element.getAttribute("workQueueSize");
            if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
                //
            }
        }

        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        return beanDefinition;
    }

    private static String resolveReference(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
            String valueInCache = configManager.getStringValue(value.substring(2, value.length() - 1));
            if (valueInCache == null) {
                throw new IllegalStateException("undefined config property:" + element.getAttribute(attribute));
            } else {
                value = valueInCache;
            }
        }
        return value;
    }
}
