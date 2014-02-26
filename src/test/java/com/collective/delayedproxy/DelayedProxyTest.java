package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.net.Socket;

import static junit.framework.Assert.*;

public class DelayedProxyTest {

    final static int SERVER_PORT = 2000;
    final static int CLIENT_PORT = 2001;
    final static String HOST = "127.0.0.1";

    DelayedProxy proxy;
    Process redisProcess;

    @Before
    public void startProxy() {
        proxy = new DelayedProxy(SERVER_PORT, CLIENT_PORT).start();
    }

    @After
    public void stopProxy() {
        proxy.stop();
    }

    @Before
    public void startRedis() {
        try {
            redisProcess = new ProcessBuilder("redis-server", "--port", Integer.toString(SERVER_PORT)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void stopRedis() {
        redisProcess.destroy();
    }


    @Test
    public void testProxyInit() {
        assertEquals(SERVER_PORT, proxy.getServerPort());
        assertEquals(CLIENT_PORT, proxy.getForwardPort());
    }

    @Test
    public void testProxyShutdown() {
        proxy.stop();
        assertFalse(isProxyRunning());

    }

    @Test
    public void testProxyRunning() {
        assertTrue(isProxyRunning());
    }

    private boolean isProxyRunning() {
        boolean isRunning = false;
        Socket socket = null;
        try {
            socket = new Socket(HOST, CLIENT_PORT);
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

    @Test
    public void testRedisRunning() {
        JedisPool pool = new JedisPool(HOST, SERVER_PORT);
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

    @Ignore
    public void testClientOnForwardPort() {
        JedisPool pool = new JedisPool(HOST, proxy.getForwardPort());
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }
}
