package com.collective.delayedproxy;

import com.collective.delayedproxy.util.Server;
import org.junit.Test;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class ForwardTest {

    // todo: remove this test
    @Test
    public void testClientSocket() throws Exception {

        ServerSocket socket = new ServerSocket(Config.REMOTE_PORT);
        try {
            new ProxyClient.Builder(Config.REMOTE_HOST, Config.REMOTE_PORT).build().start();
        } finally {
            socket.close();
        }
    }

    @Test
    public void testClientSocketWithServerThread() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).build());
            new ProxyClient.Builder(Config.REMOTE_HOST, Config.REMOTE_PORT).build().start();
            assertThat(serverTask.get()).isEqualTo(Boolean.FALSE);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testClientSocketWithProxyServer() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).build());

            ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            new Socket(Config.REMOTE_HOST, Config.REMOTE_PORT).close();
            assertThat(serverTask.get()).isEqualTo(Boolean.FALSE);
            proxy.stop();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testReadFromProxyServer() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).read("read test").build());

            ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket client = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            Server.write(client, "read test");
            client.close();

            assertThat(serverTask.get()).isEqualTo(Boolean.FALSE);

            proxy.stop();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testWriteToProxyServer() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).write("write test").build());

            ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket client = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            Server.read(client, "write test");
            client.close();

            assertThat(serverTask.get()).isEqualTo(Boolean.FALSE);

            proxy.stop();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testReadFromProxyServerWithDelay() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).delay(2000).start();
        try {
            Future serverTask = executor.submit(new Server.Builder(Config.REMOTE_PORT).build());
            assertThat(serverTask.get()).isEqualTo(Boolean.TRUE);
        } finally {
            proxy.stop();
            executor.shutdown();
        }
    }
}
