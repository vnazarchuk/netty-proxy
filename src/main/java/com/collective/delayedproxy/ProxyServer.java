package com.collective.delayedproxy;

import com.collective.delayedproxy.channel.ProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {

    private static final Logger log = LoggerFactory.getLogger(ProxyServer.class);
    private final int localPort;
    private final int remotePort;
    private final String remoteHost;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public ProxyServer(int localPort, String remoteHost, int remotePort) {
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public ProxyServer start() {
        log.info("Starting proxy server: port: {}", localPort);
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ProxyServerHandler(remoteHost, remotePort))
                    .childOption(ChannelOption.AUTO_READ, false);
            ChannelFuture future = bootstrap.bind(localPort).sync();
            channel = future.channel();
        } catch (InterruptedException consumed) {
            log.error("Interrupted", consumed);
            stop();
        }
        return this;
    }

    public void stop() {
        log.info("Stopping");
        if (channel != null)
            channel.close().syncUninterruptibly();
        if (bossGroup != null)
            bossGroup.shutdownGracefully().syncUninterruptibly();
        if (workerGroup != null)
            workerGroup.shutdownGracefully().syncUninterruptibly();
        log.debug("Stopped");
    }
}
