package com.archmanager_back.shared.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class LogUtils {

    private LogUtils() {
    }

    public static String userPrefix() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
        return "[user: " + username + "] ";
    }

    public static String userPrefixed(String msg) {
        return userPrefix() + msg;
    }
}
