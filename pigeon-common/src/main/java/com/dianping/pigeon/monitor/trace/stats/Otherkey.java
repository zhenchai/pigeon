package com.dianping.pigeon.monitor.trace.stats;

/**
 * @author qi.yin
 *         2016/11/17  下午1:26.
 */
public class Otherkey implements SourceKey {

    private String name;

    public Otherkey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Otherkey{" + "name=" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Otherkey otherkey = (Otherkey) o;

        return !(name != null ? !name.equals(otherkey.name) : otherkey.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
