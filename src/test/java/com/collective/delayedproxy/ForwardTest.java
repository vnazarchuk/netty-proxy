package com.collective.delayedproxy;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class ForwardTest {

    private volatile boolean failed = false;

    // todo: remove this test
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
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = service.submit(new ServerSocketTask());
            new ProxyClient(Config.REMOTE_HOST, Config.REMOTE_PORT).start();
            assertTrue(Boolean.FALSE.equals(serverTask.get()));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            service.shutdown();
        }
    }

    @Test
    public void testClientSocketWithProxyServer() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = service.submit(new ServerSocketTask());

            new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket clientSocket = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            try {
                OutputStreamWriter writer = new OutputStreamWriter(clientSocket.getOutputStream());
                writer.write("anything");
                writer.close();
            } finally {
                clientSocket.close();
            }

            assertTrue(Boolean.FALSE.equals(serverTask.get()));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            service.shutdown();
        }
    }

    private class ServerSocketTask implements Callable<Boolean> {

        public Boolean call() {
            try {
                ServerSocket socket = new ServerSocket(Config.REMOTE_PORT);
                socket.setSoTimeout(1000);
                try {
                    socket.accept();
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
            return false;
        }
    }
}
