package com.archmanager_back.shared.exception.custom;

public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException(String msg) {
        super(msg);
    }
}
