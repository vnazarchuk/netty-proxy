package com.collective.delayedproxy.channel;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProxyClientHandler extends ChannelInboundHandlerAdapter {
    private final Channel inboundChannel;

    public ProxyClientHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("PROXY CLIENT: active");
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("PROXY CLIENT: channel inactive");
        ProxyServerHandler.closeOnFlush(inboundChannel);
    }
}
