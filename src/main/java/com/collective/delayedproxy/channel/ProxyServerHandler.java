package com.collective.delayedproxy.channel;

import com.collective.delayedproxy.ProxyClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
    private final String remoteHost;
    private final int remotePort;
    ChannelFutureListener inboundListener;
    private Channel outboundChannel;
    private static final Logger log = LoggerFactory.getLogger(ProxyServerHandler.class);

    public ProxyServerHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    static void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel active");
        ChannelFuture future = new ProxyClient.Builder(remoteHost, remotePort).channel(ctx.channel()).build().start();
        outboundChannel = future.channel();
        inboundListener = new ChannelReadListener(ctx.channel());
        future.addListener(inboundListener);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Channel read");
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(inboundListener);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel inactive");
        closeOnFlush(outboundChannel);
    }
}