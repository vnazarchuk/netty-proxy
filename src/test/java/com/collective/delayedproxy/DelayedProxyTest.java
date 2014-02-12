package com.collective.delayedproxy;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class DelayedProxyTest {

    @Test
    public void testInit() {
        DelayedProxy proxy = new DelayedProxy(2000, 2001);
        assertEquals(2000, proxy.serverPort);
        assertEquals(2001, proxy.clientPort);
    }

}
