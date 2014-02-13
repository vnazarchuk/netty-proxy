package com.collective.delayedproxy;

import org.junit.Before;
import org.junit.Ignore;
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
        assertEquals(2000, proxy.getServerPort());
        assertEquals(2001, proxy.getClientPort());
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
    public void testRedisPort() {
        JedisPool pool = new JedisPool("127.0.0.1", proxy.getServerPort());
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

    @Ignore
    public void testClientPort() {
        JedisPool pool = new JedisPool("127.0.0.1", proxy.getClientPort());
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

}
