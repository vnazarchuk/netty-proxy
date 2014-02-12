package com.collective.delayedproxy;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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

}
