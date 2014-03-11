package com.collective.delayedproxy;

import com.collective.delayedproxy.channel.DelayHandler;
import com.collective.delayedproxy.channel.ProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {

    private static final Logger log = LoggerFactory.getLogger(ProxyServer.class);
    private static final String DELAY_HANDLER_NAME = "delay";
    private final int localPort;
    private final int remotePort;
    private final String remoteHost;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private int timeout = 0;

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
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DelayHandler(timeout), new ProxyServerHandler(remoteHost, remotePort));
                        }
                    })
                    .childOption(ChannelOption.AUTO_READ, false);
            ChannelFuture future = bootstrap.bind(localPort).sync();
            channel = future.channel();
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
            stop();
        }
        return this;
    }

    public ProxyServer delay(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public void stop() {
        log.info("Stopping proxy server");
        if (channel != null) {
            channel.close().syncUninterruptibly();
            channel = null;
        }
        if (bossGroup != null)
            bossGroup.shutdownGracefully().syncUninterruptibly();
        if (workerGroup != null)
            workerGroup.shutdownGracefully().syncUninterruptibly();
        log.info("Stopped proxy server");
    }
}
