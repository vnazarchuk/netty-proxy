package com.collective.delayedproxy;

import com.collective.delayedproxy.channel.ProxyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyClient {
    private static final Logger log = LoggerFactory.getLogger(ProxyClient.class);
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
        log.info("Starting proxy client: host: {}, port: {}", host, port);
        ChannelFuture future = new Bootstrap()
                .group(group)
                .channel(inboundChannel.getClass())
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        log.trace("Adding client handler...");
                        socketChannel.pipeline().addLast(new ProxyClientHandler(inboundChannel));
                        log.trace("Added client handler");
                    }
                })
                .connect(host, port);
        log.trace("Created client bootstrap");
        return future;
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
