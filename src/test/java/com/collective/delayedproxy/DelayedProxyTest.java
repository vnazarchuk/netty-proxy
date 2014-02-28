package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

import static junit.framework.Assert.*;

public class DelayedProxyTest {

    DelayedProxy proxy;

    @Before
    public void startProxy() {
        proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
    }

    @After
    public void stopProxy() {
        proxy.stop();
    }

    @Test
    public void testProxyInit() {
        assertEquals(Config.REMOTE_PORT, proxy.getRemotePort());
        assertEquals(Config.LOCAL_PORT, proxy.getLocalPort());
    }

    @Test
    public void testProxyShutdownAndStart() {
        proxy.stop();
        assertFalse(isSocketConnected());
        proxy.start();
        assertTrue(isSocketConnected());
    }

    @Test
    public void testProxyRunning() {
        assertTrue(isSocketConnected());
    }

    private boolean isSocketConnected() {
        boolean isRunning = false;
        Socket socket = null;
        try {
            socket = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            isRunning = socket.isConnected();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isRunning;
    }
}
