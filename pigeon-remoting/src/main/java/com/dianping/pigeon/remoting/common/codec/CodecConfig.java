package com.dianping.pigeon.remoting.common.codec;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.generic.CompressType;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.remoting.common.util.Constants;

/**
 * @author qi.yin
 *         2016/06/06  下午8:25.
 */
public class CodecConfig {

    private static final Logger logger = LoggerLoader.getLogger(CodecConfig.class);

    private ConfigManager configManager;

    private volatile boolean compressed;

    private volatile CompressType compressType;

    private volatile int compressThreshold;

    private volatile boolean checksum;

    public CodecConfig(ConfigManager configManager) {
        this.configManager = configManager;

        this.compressed = this.configManager.getBooleanValue(Constants.KEY_CODEC_COMPRESS_ENABLE,
                Constants.DEFAULT_CODEC_COMPRESS_ENABLE);

        this.compressType = getCompressType((byte) this.configManager.getIntValue(Constants.KEY_CODEC_COMPRESS_TYPE,
                Constants.DEFAULT_CODEC_COMPRESS_TYPE));

        this.compressThreshold = this.configManager.getIntValue(Constants.KEY_CODEC_COMPRESS_THRESHOLD,
                Constants.DEFAULT_CODEC_COMPRESS_THRESHOLD);

        this.checksum = this.configManager.getBooleanValue(Constants.KEY_CODEC_CHECKSUM_ENABLE,
                Constants.DEFAULT_CODEC_CHECKSUM_ENABLE);

        configManager.registerConfigChangeListener(new InnerConfigChangeListener());
    }

    public boolean isCompress(int frameSize) {
        if (compressed) {

            if (frameSize > compressThreshold) {
                return true;
            }
        }
        return false;
    }


    public CompressType getCompressType(){
        return compressType;
    }

    private final CompressType getCompressType(byte code) {

        CompressType compressType = CompressType.None;

        try {
            compressType = CompressType.getCompressType(code);
        } catch (Exception e) {
            logger.error("Invalid compressType. code:" + code, e);
        }

        return compressType;

    }

    public boolean isChecksum() {
        return checksum;
    }

    private class InnerConfigChangeListener implements ConfigChangeListener {

        @Override
        public void onKeyUpdated(String key, String value) {
            if (key.endsWith(Constants.KEY_CODEC_COMPRESS_ENABLE)) {
                try {
                    compressed = Boolean.valueOf(value);
                } catch (RuntimeException e) {
                }
            } else if (key.endsWith(Constants.KEY_CODEC_COMPRESS_TYPE)) {
                try {
                    compressType = getCompressType(Byte.valueOf(value));
                } catch (RuntimeException e) {
                }
            } else if (key.endsWith(Constants.KEY_CODEC_COMPRESS_THRESHOLD)) {
                try {
                    compressThreshold = Integer.valueOf(value);
                } catch (RuntimeException e) {
                }
            } else if (key.endsWith(Constants.KEY_CODEC_CHECKSUM_ENABLE)) {
                try {
                    checksum = Boolean.valueOf(value);
                } catch (RuntimeException e) {
                }
            }
        }

        @Override
        public void onKeyAdded(String key, String value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onKeyRemoved(String key) {
            // TODO Auto-generated method stub

        }

    }

}
