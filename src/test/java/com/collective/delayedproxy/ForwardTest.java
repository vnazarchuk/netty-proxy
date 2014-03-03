package com.collective.delayedproxy;

import com.collective.delayedproxy.util.Server;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class ForwardTest {

    // todo: remove this test
    @Test
    public void testClientSocket() {
        java.net.ServerSocket socket = null;
        try {
            socket = new java.net.ServerSocket(Config.REMOTE_PORT);
            new ProxyClient.Builder(Config.REMOTE_HOST, Config.REMOTE_PORT).build().start();
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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).build());
            new ProxyClient.Builder(Config.REMOTE_HOST, Config.REMOTE_PORT).build().start();
            assertTrue(Boolean.FALSE.equals(serverTask.get()));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testClientSocketWithProxyServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).build());

            DelayedProxy proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT).close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));
            proxy.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testReadFromProxyServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).read("read test").build());

            DelayedProxy proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket client = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            Server.write(client, "read test");
            client.close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));

            proxy.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testWriteToProxyServer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).write("write test").build());

            DelayedProxy proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket client = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);

            System.out.println("PROXY SERVER CLIENT: connected");

            Server.read(client, "write test");
            client.close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));

            proxy.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            executor.shutdown();
        }
    }
}
