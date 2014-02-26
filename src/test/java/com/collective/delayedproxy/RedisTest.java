package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static junit.framework.Assert.assertNotNull;

public class RedisTest {

    Process redisProcess;

    @Before
    public void startRedis() {
        try {
            redisProcess = new ProcessBuilder("redis-server", "--port", Integer.toString(DelayedProxyTest.REMOTE_PORT)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void stopRedis() {
        redisProcess.destroy();
    }

    @Test
    public void testRedisRunning() {
        JedisPool pool = new JedisPool(DelayedProxyTest.REMOTE_HOST, DelayedProxyTest.REMOTE_PORT);
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

    @Ignore
    public void testClientOnForwardPort() {
        JedisPool pool = new JedisPool(DelayedProxyTest.REMOTE_HOST, DelayedProxyTest.LOCAL_PORT);
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }
}
