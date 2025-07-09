// src/main/java/com/example/util/PortUtils.java
package com.archmanager_back.util;

import java.io.IOException;
import java.net.ServerSocket;

public final class PortUtils {

    private PortUtils() {
        // Utility class
    }

    /**
     * Trouve un port TCP libre sur la machine locale.
     *
     * @return un port libre (>=1024 et <=65535)
     * @throws IllegalStateException si impossible d’ouvrir un ServerSocket
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find a free TCP port", e);
        }
    }
}
