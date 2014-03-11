package com.collective.delayedproxy.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ProxyClientHandler.class);
    private final Channel inboundChannel;
    private ChannelFutureListener outboundListener;

    public ProxyClientHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
        log.trace("Created client handler");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.trace("Channel active");
        outboundListener = new ChannelReadListener(ctx.channel());
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.trace("Channel inactive");
        ProxyServerHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.trace("Channel read");
        ByteBuf buf = (ByteBuf) msg;
        log.info("Chunk length: {}", buf.readableBytes());
        inboundChannel.writeAndFlush(msg).addListener(outboundListener);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught", cause);
        ProxyServerHandler.closeOnFlush(ctx.channel());
    }
}
