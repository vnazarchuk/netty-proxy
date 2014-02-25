package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

import static junit.framework.Assert.*;

public class DelayedProxyTest {

    final static int SERVER_PORT = 2000;
    final static int CLIENT_PORT = 2001;
    final static String HOST = "127.0.0.1";

    DelayedProxy proxy;
    Process redisProcess;

    @Before
    public void initProxy() {
        proxy = new DelayedProxy(2000, 2001);
    }

    @Before
    public void runRedis() {
        try {
            redisProcess = new ProcessBuilder("redis-server", "--port", Integer.toString(SERVER_PORT)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void stopRedis() {
        redisProcess.destroy();
    }


    @Test
    public void testInit() {
        assertEquals(SERVER_PORT, proxy.getServerPort());
        assertEquals(CLIENT_PORT, proxy.getForwardPort());
    }

    @Test
    public void testStartup() {
        proxy.start();
        assertTrue(proxy.isRunning);
    }

    @Test
    public void testIsNotRunning() {
        assertFalse(proxy.isRunning);
    }

    @Test
    public void testClientOnServerPort() {
        JedisPool pool = new JedisPool(HOST, proxy.getServerPort());
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
