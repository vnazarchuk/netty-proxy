package com.collective.delayedproxy;

import com.collective.delayedproxy.channel.DelayHandler;
import com.collective.delayedproxy.channel.ProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {

    private static final Logger log = LoggerFactory.getLogger(ProxyServer.class);
    private final int localPort;
    private final int remotePort;
    private final String remoteHost;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private long timeout = 0;

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
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DelayHandler(timeout), new ProxyServerHandler(remoteHost, remotePort));
                        }
                    })
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(localPort).sync();
        } catch (InterruptedException e) {
            log.error("Can't start", e);
            stop();
        }
        return this;
    }

    public ProxyServer delay(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public void stop() {
        log.info("Stopping proxy server");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup = null;
        }
        log.info("Stopped proxy server");
    }
}
