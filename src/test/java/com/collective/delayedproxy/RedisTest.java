package com.collective.delayedproxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

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
        log.trace("Created pool");
        Jedis jedis = pool.getResource();
        pool.returnResource(jedis);
        pool.destroy();
        assertThat(jedis).isNotNull();
    }

    @Test
    public void testClientOnForwardPort() {
        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        JedisPool pool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        log.trace("Created pool");
        Jedis jedis = pool.getResource();
        pool.returnResource(jedis);
        pool.destroy();
        proxy.stop();
        assertThat(jedis).isNotNull();
    }

    @Test
    public void testPortForwarding() {

        JedisPool remotePool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        log.trace("Pool: {}", remotePool.toString());
        log.trace("Created remote pool");
        Jedis remoteJedis = remotePool.getResource();
        log.trace("Created remote jedis");

        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        log.trace("Created proxy server");

        JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);
        Jedis localJedis = localPool.getResource();
        log.trace("Created local jedis resources");

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
                    log.trace("Getting value...");
                    String readValue = localJedis.get(key + String.valueOf(j));
                    log.trace("Got value");
                    localPool.returnResource(localJedis);
                    return readValue;
                }
            });

            String readValue = (String) result.get();
            log.info("Value #{}: {}", j, readValue);
            assertThat(readValue).isEqualTo(originalValue + j);
        }

        // tear down
        remotePool.returnResource(remoteJedis);
        localPool.destroy();
        remotePool.destroy();
        proxy.stop();
    }

    @Test
    public void testWithMultipleClientsConcurrently() throws Exception {
        // set up
        JedisPool remotePool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        Jedis remoteJedis = remotePool.getResource();
        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        final JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);

        final String key = "key";
        final String originalValue = "value";
        final int threadCount = 32;
        Map<String, String> map = new HashMap<String, String>(threadCount);
        final Map<String, String> resultMap = new ConcurrentHashMap<String, String>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            String k = key + i;
            String v = originalValue + i;
            map.put(k, v);
            resultMap.put(k, "");
            remoteJedis.set(k, v);
        }
        assertThat(resultMap).isNotEqualTo(map);

        // run test
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (final String k : resultMap.keySet()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Jedis localJedis = localPool.getResource();
                    log.trace("Getting value with key: {}...", k);
                    String v = localJedis.get(k);
                    log.trace("Got value: {}:{}", k, v);
                    localPool.returnResource(localJedis);
                    resultMap.put(k, v);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // verify
        assertThat(resultMap).isEqualTo(map);

        // tear down
        proxy.stop();
        remotePool.returnResource(remoteJedis);
        localPool.destroy();
        remotePool.destroy();
    }

    @Test
    public void testWithMultipleClientsConcurrentlyAndLargeData() throws Exception {
        // set up
        JedisPool remotePool = new JedisPool(Config.REMOTE_HOST, Config.REMOTE_PORT);
        Jedis remoteJedis = remotePool.getResource();
        ProxyServer proxy = new ProxyServer(Config.LOCAL_PORT, Config.REMOTE_HOST, Config.REMOTE_PORT).start();
        final JedisPool localPool = new JedisPool(Config.REMOTE_HOST, Config.LOCAL_PORT);

        final String key = "key";
        final int threadCount = 500;
        Map<byte[], byte[]> map = new HashMap<byte[], byte[]>(threadCount);
        final Map<byte[], byte[]> resultMap = new ConcurrentHashMap<byte[], byte[]>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            byte[] k = (key + i).getBytes();
            byte[] v = ByteBuffer.allocate(4000).putInt(i).array();
            map.put(k, v);
            resultMap.put(k, ByteBuffer.allocate(4).putInt(-1).array());
            remoteJedis.set(k, v);
        }

        // Initial check to confirm values aren't read from Server. Redundant but makes me feel better at the moment
        Set<byte[]> ks = map.keySet();
        Set<byte[]> rKeys = resultMap.keySet();
        assertThat(rKeys).isEqualTo(ks);
        for (byte[] k : resultMap.keySet()) {
            assertThat(resultMap.get(k)).isNotEqualTo(map.get(k));
        }

        // run test
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (final byte[] k : resultMap.keySet()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Jedis localJedis = localPool.getResource();
                    log.trace("Getting value with key: {}...", k);
                    byte[] v = localJedis.get(k);
//                    log.trace("Got value: {}:{}", k, v);
                    log.trace("Array length: {}", v.length);
                    localPool.returnResource(localJedis);
                    resultMap.put(k, v);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // verify
        Set<byte[]> keys = map.keySet();
        Set<byte[]> resultKeys = resultMap.keySet();
        assertThat(resultKeys).isEqualTo(keys);
        for (byte[] k : resultMap.keySet()) {
            assertThat(resultMap.get(k)).isEqualTo(map.get(k));
        }

        // tear down
        proxy.stop();
        remotePool.returnResource(remoteJedis);
        localPool.destroy();
        remotePool.destroy();
    }

    @Test
    @Ignore("TBD")
    public void testDelay() {

    }
}
