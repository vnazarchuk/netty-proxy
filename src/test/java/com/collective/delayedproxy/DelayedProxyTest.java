package com.collective.delayedproxy;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static junit.framework.Assert.*;

public class DelayedProxyTest {

    DelayedProxy proxy;

    @Before
    public void initProxy() {
        proxy = new DelayedProxy(2000, 2001);
    }


    @Test
    public void testInit() {
        assertEquals(2000, proxy.serverPort);
        assertEquals(2001, proxy.clientPort);
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
    public void testClientConnection() {
        JedisPool pool = new JedisPool("127.0.0.1", 2000);
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

}
