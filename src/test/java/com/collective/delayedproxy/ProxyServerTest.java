package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

public class ProxyServerTest {

    ProxyServer proxy;

    @Before
    public void start() {
        proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
    }

    @After
    public void stop() {
        proxy.stop();
    }

    @Test
    public void init() {
        assertThat(proxy.getRemotePort()).isEqualTo(Config.REMOTE_PORT);
        assertThat(proxy.getLocalPort()).isEqualTo(Config.LOCAL_PORT);
    }

    @Test
    public void shutdownAndStart() {
        proxy.stop();
        assertThat(isSocketConnected()).isFalse();
        proxy.start();
        assertThat(isSocketConnected()).isTrue();
    }

    @Test
    public void isRunning() {
        assertThat(isSocketConnected()).isTrue();
    }

    private boolean isSocketConnected() {
        try {
            Socket socket = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            try {
                socket.isConnected();
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
