package com.qin.heart;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HeartBizClientHandler extends SimpleChannelInboundHandler<String> {

    private final AtomicInteger num = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.warn("成功连接服务端");
        ctx.executor().schedule(() -> {
                    final var count = num.incrementAndGet();
                    long data = System.currentTimeMillis();
                    log.warn("发送数据start:" + data + "  次数" + count);
                    ctx.pipeline().writeAndFlush(String.valueOf(data));
                    log.warn("发送数据end:" + data + "  次数" + count);
                }, 1,TimeUnit.SECONDS)
                .addListener(future -> {
                    Throwable cause = future.cause();
                    if (cause != null) {
                        log.warn("发送失败：" + cause.getMessage());
                        cause.printStackTrace();
                    } else {
                        log.warn("发送成功");
                    }
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.warn("接收服务端消息:" + msg);
    }

}
