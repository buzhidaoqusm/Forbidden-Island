package com.island.config;

public class NetworkConfig {
    private final int listenPort;

    public NetworkConfig(int listenPort) { this.listenPort = listenPort; }

    public int getListenPort() {
        return listenPort;
    }
}
