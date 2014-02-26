package com.collective.delayedproxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ProxyClient {
    private final String host;
    private final int port;

    public ProxyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ProxyClient start() {
        try {

            Bootstrap bootstrap = new Bootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter());
            ChannelFuture future = bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {

        }
        return this;
    }
}
