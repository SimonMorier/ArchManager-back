package com.archmanager_back.exception.custom;

public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException(String msg) {
        super(msg);
    }
}
