package com.dianping.pigeon.remoting.common.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/01  下午4:37.
 */
public class MethodKey implements SourceKey, DestinationKey {

    private String serviceName;

    private String methodName;

    public MethodKey() {
    }

    public MethodKey(String serviceName, String methodName) {
        this.serviceName = serviceName;
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String jsonMapKey() {
        return "MethodKey{serviceName=" + serviceName + ", methodName=" + methodName + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodKey that = (MethodKey) o;

        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        return !(methodName != null ? !methodName.equals(that.methodName) : that.methodName != null);

    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        return result;
    }

    //attention, json depend on toString, so do not change at will
    @Override
    public String toString() {
        return jsonMapKey();
    }
}
