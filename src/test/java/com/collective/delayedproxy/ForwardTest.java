package com.collective.delayedproxy;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

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
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    class ServerSocketThread extends Thread {

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ServerSocket socket = new ServerSocket(Config.REMOTE_PORT);
                    socket.setSoTimeout(1000);
                    try {
                        socket.accept();
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                        fail();
                    } catch (IOException e) {
                        e.printStackTrace();
                        fail();
                    } finally {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    fail();
                } finally {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
