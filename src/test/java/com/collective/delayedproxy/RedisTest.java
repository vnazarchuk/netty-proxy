package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
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
        try {
            assertNotNull(jedis);
        } catch (JedisConnectionException e) {
            e.printStackTrace();
            fail();
        } finally {
            if (jedis != null) {
                pool.returnResource(jedis);
            }
            pool.destroy();
        }
    }

    @Test
    public void testClientOnForwardPort() {
        DelayedProxy proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis jedis = pool.getResource();
        try {
            assertNotNull(jedis);
        } catch (JedisConnectionException e) {
            e.printStackTrace();
            fail();
        } finally {
            proxy.stop();
            if (jedis != null) {
                pool.returnResource(jedis);
            }
            pool.destroy();
        }
    }

    @Test
    public void testPortForwarding() {
        JedisPool remotePool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        Jedis remoteJedis = remotePool.getResource();

        DelayedProxy proxy = new DelayedProxy(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

        JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis localJedis = localPool.getResource();
        try {
            remoteJedis.set("key1", "lorem ipsum...");

            String value = localJedis.get("key1");
            System.out.println("REDIS: test value: " + value);

            assertTrue(value.equals("lorem ipsum..."));
        } catch (JedisConnectionException e) {
            if (remoteJedis != null) {
                remotePool.returnBrokenResource(remoteJedis);
                remoteJedis = null;
            }
            if (localJedis != null) {
                localPool.returnBrokenResource(localJedis);
                localJedis = null;
            }
            e.printStackTrace();
            fail();
        } finally {
            proxy.stop();
            if (remoteJedis != null) {
                remotePool.returnResource(remoteJedis);
            }
            if (localJedis != null) {
                localPool.returnResource(localJedis);
            }
            localPool.destroy();
            remotePool.destroy();
        }
    }
}
