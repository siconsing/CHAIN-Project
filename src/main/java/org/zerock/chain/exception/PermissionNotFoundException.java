package org.zerock.chain.exception;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException() {
        super();
    }

    public PermissionNotFoundException(String message) {
        super(message);
    }

    public PermissionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionNotFoundException(Throwable cause) {
        super(cause);
    }

    protected PermissionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
