package com.collective.delayedproxy;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    // todo: remove this test
    @Test
    public void testClientSocket() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(Config.REMOTE_PORT);
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
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = service.submit(new ServerSocketTask());
            new ProxyClient.Builder(Config.REMOTE_HOST, Config.REMOTE_PORT).build().start();
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

            DelayedProxy proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT).close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));
            proxy.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            service.shutdown();
        }
    }

    @Test
    public void testReadFromProxyServer() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Future serverTask = service.submit(new ServerSocketTask().withRead());

            DelayedProxy proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

            Socket client = new Socket(Config.REMOTE_HOST, Config.LOCAL_PORT);
            System.out.println("PROXY SERVER CLIENT: connected");
            OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
            System.out.println("PROXY SERVER CLIENT: writing");
            writer.write("anything");
            System.out.println("PROXY SERVER CLIENT: closing");
            writer.close();
            client.close();

            assertTrue(Boolean.FALSE.equals(serverTask.get()));

            proxy.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            service.shutdown();
        }
    }

    private class ServerSocketTask implements Callable<Boolean> {

        private ServerSocketReader reader = null;

        public Boolean call() {
            try {
                ServerSocket socket = new ServerSocket(Config.REMOTE_PORT);
                socket.setSoTimeout(1000);
                try {
                    System.out.println("SERVER: waiting to accept");
                    Socket client = socket.accept();
                    if (reader != null) {
                        reader.read(client);
                    }
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
            return false;
        }

        public ServerSocketTask withRead() {
            reader = new ServerSocketReader();
            return this;
        }

        private class ServerSocketReader {

            public void read(Socket socket) throws IOException {
                socket.setSoTimeout(1000);
                System.out.println("SERVER: accepted");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("SERVER: reading");
                System.out.println(reader.readLine());
                reader.close();
            }
        }
    }
}
