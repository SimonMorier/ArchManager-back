package com.archmanager_back.exception.custom;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid username or password.");
    }
}