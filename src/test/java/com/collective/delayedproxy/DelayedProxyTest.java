package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

import static junit.framework.Assert.*;

public class DelayedProxyTest {

    final static int LOCAL_PORT = 2001;
    final static int REMOTE_PORT = 2000;
    final static String REMOTE_HOST = "127.0.0.1";

    DelayedProxy proxy;

    @Before
    public void startProxy() {
        proxy = new DelayedProxy(LOCAL_PORT, REMOTE_PORT).start();
    }

    @After
    public void stopProxy() {
        proxy.stop();
    }

    @Test
    public void testClientSocket() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket();
            new ProxyClient(REMOTE_HOST, REMOTE_PORT).start();
        } catch (ConnectException e) {
            fail();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testProxyInit() {
        assertEquals(REMOTE_PORT, proxy.getRemotePort());
        assertEquals(LOCAL_PORT, proxy.getLocalPort());
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
            socket = new Socket(REMOTE_HOST, LOCAL_PORT);
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
