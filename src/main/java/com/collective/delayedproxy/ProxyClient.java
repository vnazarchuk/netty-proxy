package com.collective.delayedproxy;

import com.collective.delayedproxy.channel.ProxyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ProxyClient {
    private final String host;
    private final int port;
    private final Channel inboundChannel;
    private final EventLoopGroup group;

    private ProxyClient(Builder builder) {
        host = builder.host;
        port = builder.port;
        inboundChannel = builder.inboundChannel;
        group = builder.group;
    }

    public ChannelFuture start() {
        System.out.println("PROXY CLIENT: starting");
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(inboundChannel.getClass())
                    .option(ChannelOption.AUTO_READ, false)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ProxyClientHandler(inboundChannel));
                        }
                    });
            return bootstrap.connect(host, port).sync();
        } catch (InterruptedException consumed) {

        }
        return null;
    }

    public static class Builder {

        private final String host;
        private final int port;

        private Channel inboundChannel = new NioSocketChannel();
        private EventLoopGroup group = new NioEventLoopGroup();

        public Builder(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public Builder channel(Channel inboundChannel) {
            this.inboundChannel = inboundChannel;
            this.group = inboundChannel.eventLoop();
            return this;
        }

        public ProxyClient build() {
            return new ProxyClient(this);
        }
    }
}
