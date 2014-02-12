package com.collective.delayedproxy;

public class DelayedProxy {

    private int serverPort;
    private int clientPort;
    public boolean isRunning;

    public DelayedProxy(int serverPort, int clientPort) {
        this.serverPort = serverPort;
        this.clientPort = clientPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void start() {
        isRunning = true;
    }
}
