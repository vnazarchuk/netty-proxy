package com.collective.delayedproxy.channel;

import com.collective.delayedproxy.ProxyClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
    private final String remoteHost;
    private final int remotePort;
    ChannelFutureListener inboundListener;
    private Channel outboundChannel;

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
        System.out.println("PROXY SERVER: channel active");
        ChannelFuture future = new ProxyClient.Builder(remoteHost, remotePort).channel(ctx.channel()).build().start();
        outboundChannel = future.channel();
        inboundListener = new ChannelReadListener(ctx.channel());
        future.addListener(inboundListener);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("PROXY SERVER: channel read");
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(inboundListener);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("PROXY SERVER: channel inactive");
        closeOnFlush(outboundChannel);
    }
}