package com.dianping.pigeon.remoting.provider.config.spring;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.provider.config.PoolConfigFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

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
        beanDefinition.setBeanClass(PoolBean.class);
        MutablePropertyValues properties = beanDefinition.getPropertyValues();

        String id = element.getAttribute("id");
        properties.addPropertyValue("poolName", id);

        Integer corePoolSize = Integer.parseInt(resolveReference(element, "corePoolSize"));
        properties.addPropertyValue("corePoolSize", corePoolSize);
        String value = element.getAttribute("corePoolSize");
        if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
            PoolConfigFactory.getCoreSizeKeys().put(id, value.substring(2, value.length() - 1));
        }

        Integer maxPoolSize = Integer.parseInt(resolveReference(element, "maxPoolSize"));
        properties.addPropertyValue("maxPoolSize", maxPoolSize);
        value = element.getAttribute("maxPoolSize");
        if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
            PoolConfigFactory.getMaxSizeKeys().put(id, value.substring(2, value.length() - 1));
        }

        Integer workQueueSize = Integer.parseInt(resolveReference(element, "workQueueSize"));
        properties.addPropertyValue("workQueueSize", workQueueSize);
        value = element.getAttribute("workQueueSize");
        if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
            PoolConfigFactory.getQueueSizeKeys().put(id, value.substring(2, value.length() - 1));
        }

        if (corePoolSize < 0 ||
                maxPoolSize <= 0 ||
                maxPoolSize < corePoolSize ||
                workQueueSize <= 0)
            throw new IllegalArgumentException("please check pool config: " + id);

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
