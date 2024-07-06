package com.qin.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HeartBeatClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (msg != null && msg.equals("idle close")) {
            log.error(" 服务端关闭连接，客户端也关闭");
            ctx.channel().closeFuture();
         } else {
            // 往下传
            ctx.fireChannelRead(msg);
        }

    }

    private final AtomicInteger restart = new AtomicInteger(0);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("连接断开，IP端口为：{}", ctx.channel().remoteAddress());
        //使用过程中断线重连
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(() -> {
            log.error("连接断开 schedule 重启");
            if (restart.incrementAndGet() < 10) {
            } else {
                log.error("重启次数过多");
            }
        },2L,TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }
}
