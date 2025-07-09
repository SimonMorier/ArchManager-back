package com.archmanager_back.exception;

public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException(String msg) {
        super(msg);
    }
}
