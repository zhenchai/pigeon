package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.util.Pair;

/**
 * @author qi.yin
 *         2016/11/30  下午4:03.
 */
public class PairKey<K extends KeyJsonable, V extends KeyJsonable> extends Pair<K, V> implements KeyJsonable {

    public PairKey() {
        super();
    }

    public PairKey(K key, V value) {
        super(key, value);
    }

    @Override
    public String jsonMapKey() {
        return "PairKey{" + "key=" + getKey().jsonMapKey() + ", value=" + getValue().jsonMapKey() + "}";
    }

    //attention!!! map key to json depend on toString, so do not change at will.
    @Override
    public String toString() {
        return jsonMapKey();
    }
}
