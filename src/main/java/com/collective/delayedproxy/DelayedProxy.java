package com.collective.delayedproxy;

public class DelayedProxy {

    private int serverPort;
    private int forwardPort;
    public boolean isRunning;

    public DelayedProxy(int serverPort, int forwardPort) {
        this.serverPort = serverPort;
        this.forwardPort = forwardPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getForwardPort() {
        return forwardPort;
    }

    public void start() {
        isRunning = true;
    }
}
