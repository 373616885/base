package com.qin.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConnectionListener implements ChannelFutureListener {

    private final Bootstrap bootstrap;

    public ConnectionListener(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    private static final AtomicInteger restart = new AtomicInteger(0);
    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            log.warn("-------------客户端重新连接-----------------");
            final var channel = future.channel();
            final EventLoop loop = future.channel().eventLoop();
            loop.schedule(() -> {
                if (restart.incrementAndGet() < 5) {
                    try {
                        //重连
                        bootstrap.connect(channel.remoteAddress()).addListener(new ConnectionListener(bootstrap));
                    } catch (Exception e) {
                        log.error("重连失败");
                    }
                } else {
                    log.error("重启次数过多");
                    //释放NIO线程组
                    loop.parent().shutdownGracefully();
                }
            },2, TimeUnit.SECONDS);
        } else {
            log.warn("-------------客户端连接成功-----------------");
        }
    }

}
