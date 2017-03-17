package com.dianping.pigeon.remoting.invoker.domain;

/**
 * Created by chenchongze on 17/3/16.
 */
public class DegradeInfo {

    private boolean isDegrade = false;

    private boolean isFailureDegrade = false;

    private Object cause;

    public boolean isDegrade() {
        return isDegrade;
    }

    public void setDegrade(boolean degrade) {
        isDegrade = degrade;
    }

    public boolean isFailureDegrade() {
        return isFailureDegrade;
    }

    public void setFailureDegrade(boolean failureDegrade) {
        isFailureDegrade = failureDegrade;
    }

    public Object getCause() {
        return cause;
    }

    public void setCause(Object cause) {
        this.cause = cause;
    }
}
