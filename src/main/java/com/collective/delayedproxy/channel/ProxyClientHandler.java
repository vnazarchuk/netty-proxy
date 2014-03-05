package com.collective.delayedproxy.channel;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyClientHandler extends ChannelInboundHandlerAdapter {
    private final Channel inboundChannel;
    private static final Logger log = LoggerFactory.getLogger(ProxyClientHandler.class);

    public ProxyClientHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel active");
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel inactive");
        ProxyServerHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Channel read");
        inboundChannel.writeAndFlush(msg).addListener(new ChannelReadListener(ctx.channel()));
    }
}
