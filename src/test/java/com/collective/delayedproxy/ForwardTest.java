package com.collective.delayedproxy;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class ForwardTest {

    private volatile boolean failed = false;

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

    class ServerSocketTask implements Callable<Boolean> {

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
