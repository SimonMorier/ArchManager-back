package com.archmanager_back.util;

import java.io.IOException;
import java.net.ServerSocket;

import lombok.AllArgsConstructor;

@AllArgsConstructor()
public final class PortUtils {

    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find a free TCP port", e);
        }
    }
}
