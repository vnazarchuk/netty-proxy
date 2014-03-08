package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisTest {

    private static final Logger log = LoggerFactory.getLogger(RedisTest.class);
    Process redisProcess;

    @Before
    public void startRedis() {
        try {
            log.info("Starting Redis... on port: {}", Config.REMOTE_PORT);
            redisProcess = new ProcessBuilder("redis-server", "--port", Integer.toString(Config.REMOTE_PORT)).start();
            // Redis throws redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool
            // when running many tests all at once
            Thread.sleep(3000);
            log.info("Redis started");
        } catch (Exception e) {
            log.error("Can't run Redis", e);
        }
    }

    @After
    public void stopRedis() {
        log.info("Stopping Redis...");
        redisProcess.destroy();
        log.info("Redis stopped");
    }

    @Test
    public void isRedisRunning() {
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        log.trace("created pool");
        Jedis jedis = pool.getResource();
        pool.returnResource(jedis);
        pool.destroy();
        assertThat(jedis).isNotNull();
    }

    @Test
    public void testClientOnForwardPort() {
        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        log.trace("created pool");
        Jedis jedis = pool.getResource();
        pool.returnResource(jedis);
        pool.destroy();
        proxy.stop();
        assertThat(jedis).isNotNull();
    }

    @Test
    public void testPortForwarding() {

        JedisPool remotePool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        log.trace("pool: {}", remotePool.toString());
        log.trace("created remote pool");
        Jedis remoteJedis = remotePool.getResource();
        log.trace("created remote jedis");

        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        log.trace("created proxy server");

        JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis localJedis = localPool.getResource();
        log.trace("created local jedis resources");

        remoteJedis.set("key1", "lorem ipsum...");

        String value = localJedis.get("key1");

        remotePool.returnResource(remoteJedis);
        localPool.returnResource(localJedis);
        remotePool.destroy();
        localPool.destroy();
        proxy.stop();

        assertThat(value).isEqualTo("lorem ipsum...");
    }

    @Test
    public void testWithMultipleClients() throws Exception {

        // set up
        JedisPool remotePool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        Thread.sleep(1000);
        Jedis remoteJedis = remotePool.getResource();
        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        final JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        final String key = "key";
        final String originalValue = "value";

        // run test
        int threadCount = 32;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int j = i;
            remoteJedis.set(key + j, originalValue + j);
            Future result = executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Jedis localJedis = localPool.getResource();
                    log.trace("getting value...");
                    String readValue = localJedis.get((key + String.valueOf(j)));
                    log.trace("got value");
//                    log.info("array length: {}", readValue);
                    localPool.returnResource(localJedis);
                    return readValue;
                }
            });

            String readValue = (String) result.get();
            log.info("value #{}: {}", j, readValue);
            assertThat(readValue).isEqualTo(originalValue + j);
        }

        // tear down
        remotePool.returnResource(remoteJedis);
        localPool.destroy();
        remotePool.destroy();
        proxy.stop();
    }
}
