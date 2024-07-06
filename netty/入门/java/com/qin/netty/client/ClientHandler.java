package com.qin.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private final AtomicInteger num = new AtomicInteger(0);
    private final AtomicInteger readIdleTimes = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.warn("成功连接服务端");
        //ByteBufAllocator allocator = ctx.pipeline().channel().config().getAllocator();
        //ByteBuf buffer = allocator.buffer(13);
        ByteBuf buffer = Unpooled.directBuffer(13);//重用非池的
        ctx.executor().schedule(() -> {
                    final var count = num.incrementAndGet();
                    //便于下次重用
                    buffer.retain();
                    long data = System.currentTimeMillis();
                    //写一次
                    buffer.writeBytes(String.valueOf(data).getBytes(CharsetUtil.UTF_8));
                    log.warn("发送数据start:" + data + "  次数" + count);
                    //writeAndFlush里面计数减小了1
                    ctx.pipeline().writeAndFlush(buffer);
                    buffer.clear();
                    log.warn("发送数据end:" + data + "  次数" + count);

                }, 1, TimeUnit.SECONDS)
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent stateEvent) {
            String eventType = null;
            switch (stateEvent.state()) {
                case READER_IDLE -> {
                    eventType = "读空闲";
                    //超过3次空闲
                    readIdleTimes.incrementAndGet(); // 读空闲的计数加1
                }
                case WRITER_IDLE -> eventType = "写空闲";
                // 不处理
                case ALL_IDLE -> eventType = "读写空闲";
                // 不处理
            }
            log.warn(ctx.channel().remoteAddress() + "超时事件：" + eventType);
            if (readIdleTimes.get() > 3) {
                log.warn(" [server]读空闲超过3次，关闭连接，释放更多资源");
                ctx.channel().writeAndFlush("idle close");
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
