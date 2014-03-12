package com.collective.delayedproxy.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DelayHandler.class);
    private final long timeout;

    public DelayHandler(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Waiting {} ms...", timeout);
        Thread.sleep(timeout);
        super.channelRead(ctx, msg);
    }
}
