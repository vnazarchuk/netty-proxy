package com.collective.delayedproxy;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static junit.framework.Assert.fail;

public class ForwardTest {

    @Test
    public void testClientSocket() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(Config.REMOTE_PORT);
            new ProxyClient(Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
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
    public void testClientSocketWithServerThread() {
        ServerSocketThread serverThread = new ServerSocketThread();
        try {
            serverThread.start();
            new ProxyClient(Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            serverThread.interrupt();
        }
    }

    class ServerSocketThread extends Thread {

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ServerSocket socket = new ServerSocket(Config.REMOTE_PORT);
                    try {
                        socket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                        fail();
                    } finally {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }
    }
}
