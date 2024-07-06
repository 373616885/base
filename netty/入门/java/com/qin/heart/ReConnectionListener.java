package com.qin.heart;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ReConnectionListener implements ChannelFutureListener {

    private final HeartClient client;

    public ReConnectionListener(HeartClient client) {
        this.client = client;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            log.warn("客户端连接成功");
            return;
        }

        // 需要重连
        final var eventLoop = future.channel().eventLoop();

        eventLoop.schedule(() -> {
            // 释放连接
            future.channel().close();
            client.reConect();

        }, 2, TimeUnit.SECONDS);
    }
}
