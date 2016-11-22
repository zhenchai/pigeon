package com.dianping.pigeon.remoting.common.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/17  下午4:27.
 */
public class KeyPair<S extends SourceKey, D extends DestinationKey> {

    private SourceKey srcKey;

    private DestinationKey dstKey;

    public KeyPair(SourceKey srcKey, DestinationKey dstKey) {
        this.srcKey = srcKey;
        this.dstKey = dstKey;
    }

    public SourceKey getSrcKey() {
        return srcKey;
    }

    public void setSrcKey(SourceKey srcKey) {
        this.srcKey = srcKey;
    }

    public DestinationKey getDstKey() {
        return dstKey;
    }

    public void setDstKey(DestinationKey destKey) {
        this.dstKey = destKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyPair<?, ?> keyPair = (KeyPair<?, ?>) o;

        if (srcKey != null ? !srcKey.equals(keyPair.srcKey) : keyPair.srcKey != null) return false;
        return !(dstKey != null ? !dstKey.equals(keyPair.dstKey) : keyPair.dstKey != null);
    }

    @Override
    public int hashCode() {
        int result = srcKey != null ? srcKey.hashCode() : 0;
        result = 31 * result + (dstKey != null ? dstKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyPair{" + "srcKey=" + srcKey + ", dstKey=" + dstKey + "}";
    }
}
