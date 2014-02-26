package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import static junit.framework.Assert.*;

public class RedisTest {

    Process redisProcess;

    @Before
    public void startRedis() {
        try {
            redisProcess = new ProcessBuilder("redis-server", "--port", Integer.toString(Config.REMOTE_PORT)).start();
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
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

    @Ignore
    public void testClientOnForwardPort() {
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis jedis = pool.getResource();
        assertNotNull(jedis);
    }

    @Test
    public void testPortForwarding() {
        JedisPool remotePool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis remoteJedis = remotePool.getResource();
        Jedis localJedis = localPool.getResource();
        try {
            remoteJedis.set("key1", "value1");

            new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_PORT).start();

            String value = localJedis.get("key1");
            assertTrue(value.equals("value1"));
        } catch (JedisConnectionException e) {
            if (localJedis != null) {
                localPool.returnBrokenResource(localJedis);
                localJedis = null;
            }
            if (remoteJedis != null) {
                remotePool.returnBrokenResource(remoteJedis);
                remoteJedis = null;
            }
            e.printStackTrace();
            fail();
        } finally {
            if (remoteJedis != null)
                remotePool.returnResource(remoteJedis);
            if (localJedis != null)
                localPool.returnResource(localJedis);
            localPool.destroy();
            remotePool.destroy();
            redisProcess.destroy();
        }
    }
}
