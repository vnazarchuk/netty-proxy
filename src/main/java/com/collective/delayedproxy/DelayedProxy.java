package com.collective.delayedproxy;

/**
 * Created by Valeriy on 2/11/14.
 */
public class DelayedProxy {
    public int serverPort;
    public int clientPort;

    public DelayedProxy(int serverPort, int clientPort) {
        this.serverPort = serverPort;
        this.clientPort = clientPort;
    }
}
