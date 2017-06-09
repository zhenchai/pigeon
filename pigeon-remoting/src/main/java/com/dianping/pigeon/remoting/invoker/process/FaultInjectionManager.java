package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.process.filter.FaultInjectionFilter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by chenchongze on 17/4/18.
 */
public enum FaultInjectionManager {

    INSTANCE;

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private static final Logger logger = LoggerLoader.getLogger(FaultInjectionFilter.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String KEY_FAULT_INJECTION_ENABLE = "pigeon.fault.injection.enable";
    private volatile static boolean isFaultInjectionEnable = configManager.getBooleanValue(KEY_FAULT_INJECTION_ENABLE, false);
    private static final String KEY_FAULT_INJECTION_CONFIGS = "pigeon.fault.injection.configs";

    private static final Random random = new Random();
    private volatile static Map<String, FaultInjectionConfig> configMap = new HashMap<String, FaultInjectionConfig>();

    static {
        String configsStr = configManager.getStringValue(KEY_FAULT_INJECTION_CONFIGS);
        try {
            refreshFaultInjectionConfigs(configsStr);
        } catch (Throwable t) {
            logger.warn("failed to parse pigeon fault injection configs, please check!", t);
        }
        configManager.registerConfigChangeListener(new InnerGroupChangeListener());
    }

    FaultInjectionManager() {}

    public boolean isEnable(String requestKey) {
        if (isFaultInjectionEnable) {
            FaultInjectionConfig config = configMap.get(requestKey);
            if (config != null) {
                return config.getEnable();
            }
        }

        return false;
    }

    public FaultInjectionAction getAction(String requestKey) {
        FaultInjectionConfig config = configMap.get(requestKey);

        if (config != null) {
            FaultInjectionAction action = new FaultInjectionAction();
            if (!config.getSample() || (config.getSample() && random(config.getSampleRate()))) {
                action.setType(FaultInjectionType.getType(config.getType()));

                if (FaultInjectionType.DELAY.equals(action.getType())) { // 延迟类型
                    if (config.getRandomDelay()) { // 延迟时间随机
                        action.setDelay(random.nextInt(config.getMaxDelay()));
                    } else { // 延迟时间固定
                        action.setDelay(config.getMaxDelay());
                    }
                }

                return action;
            }
        }

        return new FaultInjectionAction();
    }

    public static class FaultInjectionAction {

        private FaultInjectionType type = FaultInjectionType.NONE;

        private int delay = 0;

        public FaultInjectionType getType() {
            return type;
        }

        public void setType(FaultInjectionType type) {
            this.type = type;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }

    public static class FaultInjectionConfig implements Serializable {

        private boolean enable = true;

        private String type = FaultInjectionType.EXCEPTION.getType(); // 1. exception 2. delay

        private int maxDelay; // time_unit ms

        private boolean randomDelay = false; // 是否随机延时(0-maxDelay ms)

        private boolean sample = false; // 是否按比例采样注入错误

        private float sampleRate; // 采样注入比率

        public FaultInjectionConfig() {}

        public boolean getEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getMaxDelay() {
            return maxDelay;
        }

        public void setMaxDelay(int maxDelay) {
            this.maxDelay = maxDelay;
        }

        public boolean getRandomDelay() {
            return randomDelay;
        }

        public void setRandomDelay(boolean randomDelay) {
            this.randomDelay = randomDelay;
        }

        public boolean getSample() {
            return sample;
        }

        public void setSample(boolean sample) {
            this.sample = sample;
        }

        public float getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(float sampleRate) {
            this.sampleRate = sampleRate;
        }
    }

    public enum FaultInjectionType {

        NONE("none"),
        EXCEPTION("exception"),
        DELAY("delay");

        private final String type;

        FaultInjectionType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static FaultInjectionType getType(String typeName) {
            if (NONE.getType().equals(typeName)) {
                return NONE;
            } else if (EXCEPTION.getType().equals(typeName)) {
                return EXCEPTION;
            } else if (DELAY.getType().equals(typeName)) {
                return DELAY;
            } else {
                throw new IllegalArgumentException("invalid type name: " + typeName);
            }
        }
    }

    public static class FaultInjectionException extends RuntimeException {

        public FaultInjectionException() {}

        public FaultInjectionException (String msg) {
            super(msg);
        }
    }

    private boolean random(float percent) {
        return random.nextInt(100) < percent * 100;
    }

    private static void refreshFaultInjectionConfigs(String info) throws IOException {
        if (StringUtils.isNotBlank(info)) {
            JavaType type = getCollectionType(HashMap.class, Map.class, String.class, FaultInjectionConfig.class);
            configMap = mapper.readValue(info, type);
        } else {
            configMap = new HashMap<>();
        }
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?> collectionInterfaceClass,
                                             Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametrizedType(collectionClass,
                collectionInterfaceClass, elementClasses);
    }

    private static class InnerGroupChangeListener implements ConfigChangeListener {
        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith(KEY_FAULT_INJECTION_ENABLE)) {
                try {
                    isFaultInjectionEnable = Boolean.valueOf(value);
                    logger.info("set " + KEY_FAULT_INJECTION_ENABLE + " value: " + value);
                } catch (Throwable t) {
                    logger.warn("set " + KEY_FAULT_INJECTION_ENABLE + " failed!", t);
                }
            } else if (key.endsWith(KEY_FAULT_INJECTION_CONFIGS)) {
                try {
                    refreshFaultInjectionConfigs(value);
                    logger.info("set " + KEY_FAULT_INJECTION_CONFIGS + " value: " + value);
                } catch (Throwable t) {
                    logger.warn("set " + KEY_FAULT_INJECTION_CONFIGS + " failed!", t);
                }
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {

        }

        @Override
        public void onKeyRemoved(String key) {

        }
    }
}
