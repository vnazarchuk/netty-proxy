package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import static junit.framework.Assert.*;

public class RedisTest {

    private static final Logger log = LoggerFactory.getLogger(RedisTest.class);
    Process redisProcess;

    @Before
    public void startRedis() {
        try {
            log.info("Starting Redis, port: {}", Config.REMOTE_PORT);
            redisProcess = new ProcessBuilder("redis-server", "--port", Integer.toString(Config.REMOTE_PORT)).start();
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("Couldn't start Redis", e);
        }
    }

    @After
    public void stopRedis() {
        log.info("Stopping Redis");
        redisProcess.destroy();
    }

    @Test
    public void isRedisRunning() {
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        Jedis jedis = pool.getResource();
        try {
            assertNotNull(jedis);
        } catch (JedisConnectionException e) {
            log.error("jedis error", e);
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
        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis jedis = pool.getResource();
        try {
            assertNotNull(jedis);
        } catch (JedisConnectionException e) {
            log.error("jedis error", e);
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

        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();

        JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis localJedis = localPool.getResource();
        try {
            remoteJedis.set("key1", "lorem ipsum...");

            String value = localJedis.get("key1");

            assertTrue(value.equals("lorem ipsum..."));
        } catch (JedisConnectionException e) {
            log.error("jedis error", e);
            if (remoteJedis != null) {
                remotePool.returnBrokenResource(remoteJedis);
                remoteJedis = null;
            }
            if (localJedis != null) {
                localPool.returnBrokenResource(localJedis);
                localJedis = null;
            }
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
