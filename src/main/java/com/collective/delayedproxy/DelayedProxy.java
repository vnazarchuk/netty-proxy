package com.collective.delayedproxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DelayedProxy {

    private final int serverPort;
    private final int forwardPort;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;

    public DelayedProxy(int serverPort, int forwardPort) {
        this.serverPort = serverPort;
        this.forwardPort = forwardPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getForwardPort() {
        return forwardPort;
    }

    public DelayedProxy start() {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInboundHandlerAdapter());

        ChannelFuture future = bootstrap.bind(forwardPort).awaitUninterruptibly();
        channel = future.channel();
        return this;
    }

    public void stop() {
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
