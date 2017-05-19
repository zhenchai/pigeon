package com.dianping.pigeon.remoting.invoker.exception;

import com.dianping.pigeon.remoting.common.exception.ApplicationException;

/**
 * Created by chenchongze on 17/5/19.
 */
public class ServiceFailureDegreadedException extends ApplicationException {

    public ServiceFailureDegreadedException() {
        super();
    }

    public ServiceFailureDegreadedException(String message) {
        super(message);
    }

    public ServiceFailureDegreadedException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureDegreadedException(String message, Throwable cause) {
        super(message, cause);
    }

}
