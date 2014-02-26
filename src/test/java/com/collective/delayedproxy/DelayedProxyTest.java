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

    final static int LOCAL_PORT = 2001;
    final static int REMOTE_PORT = 2000;
    final static String REMOTE_HOST = "127.0.0.1";

    DelayedProxy proxy;
    Process redisProcess;

    @Before
    public void startProxy() {
        proxy = new DelayedProxy(LOCAL_PORT, REMOTE_PORT).start();
    }

    @After
    public void stopProxy() {
        proxy.stop();
    }

    @Before
    public void startRedis() {
        try {
            redisProcess = new ProcessBuilder("redis-server", "--port", Integer.toString(REMOTE_PORT)).start();
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
        assertEquals(REMOTE_PORT, proxy.getRemotePort());
        assertEquals(LOCAL_PORT, proxy.getLocalPort());
    }

    @Test
    public void testProxyShutdownAndStart() {
        proxy.stop();
        assertFalse(isProxyRunning());
        proxy.start();
        assertTrue(isProxyRunning());
    }

    @Test
    public void testProxyRunning() {
        assertTrue(isProxyRunning());
    }

    private boolean isProxyRunning() {
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

    @Test
    public void testRedisRunning() {
        JedisPool pool = new JedisPool(REMOTE_HOST, REMOTE_PORT);
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

    @Ignore
    public void testClientOnForwardPort() {
        JedisPool pool = new JedisPool(REMOTE_HOST, proxy.getRemotePort());
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }
}
