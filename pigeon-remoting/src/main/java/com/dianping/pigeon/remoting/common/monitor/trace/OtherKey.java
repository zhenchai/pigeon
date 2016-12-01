package com.dianping.pigeon.remoting.common.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/17  下午1:26.
 */
public class OtherKey implements SourceKey {

    private String name;

    public OtherKey() {

    }

    public OtherKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String jsonMapKey() {
        return "OtherKey{" + "name=" + name + "}";
    }

    //attention, json depend on toString, so do not change at will
    @Override
    public String toString() {
        return jsonMapKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OtherKey otherkey = (OtherKey) o;

        return !(name != null ? !name.equals(otherkey.name) : otherkey.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
