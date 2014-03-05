package com.collective.delayedproxy;

import com.collective.delayedproxy.util.Server;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class ForwardTest {

    private static final Logger log = LoggerFactory.getLogger(ForwardTest.class);

    // todo: remove this test
    @Test
    public void testClientSocket() {
        java.net.ServerSocket socket = null;
        try {
            socket = new java.net.ServerSocket(Config.REMOTE_PORT);
            new ProxyClient.Builder(Config.REMOTE_HOST, Config.REMOTE_PORT).build().start();
        } catch (IOException e) {
            log.error("Couldn't open server socket", e);
            fail();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                log.error("", e);
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
            log.error("", e);
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

            ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT).close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));
            proxy.stop();
        } catch (Exception e) {
            log.error("", e);
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

            ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket client = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            Server.write(client, "read test");
            client.close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));

            proxy.stop();
        } catch (Exception e) {
            log.error("", e);
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

            ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket client = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);

            Server.read(client, "write test");
            client.close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));

            proxy.stop();
        } catch (Exception e) {
            log.error("", e);
            fail();
        } finally {
            executor.shutdown();
        }
    }
}
