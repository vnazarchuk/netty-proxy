package com.collective.delayedproxy.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class ChannelReadListener implements ChannelFutureListener {

    private final Channel channel;

    public ChannelReadListener(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            channel.read();
        } else {
            channel.close();
        }
    }
}
