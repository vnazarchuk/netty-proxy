package com.collective.delayedproxy.channel;

import com.collective.delayedproxy.ProxyClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    private final ProxyClient client;

    public ProxyServerHandler(String remoteHost, int remotePort) {
        client = new ProxyClient(remoteHost, remotePort);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        client.start();
    }
}
