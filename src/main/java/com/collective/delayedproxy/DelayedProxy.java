package com.collective.delayedproxy;

public class DelayedProxy {

    public int serverPort;
    public int clientPort;
    public boolean isRunning;

    public DelayedProxy(int serverPort, int clientPort) {
        this.serverPort = serverPort;
        this.clientPort = clientPort;
    }

    public void start() {
        isRunning = true;
    }
}
