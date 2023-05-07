package com.qin.heart;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class HeartClientStateHandler extends SimpleChannelInboundHandler<String> {

    private final HeartClient client;

    public HeartClientStateHandler(HeartClient client) {
        this.client = client;
    }

    private static final AtomicBoolean close = new AtomicBoolean(false);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        //服务端主动关闭
        if (close.get()) {
            log.warn("服务端主动关闭:" + ctx.channel().remoteAddress());
            ctx.close();
            ctx.fireChannelInactive();
            client.shutDown();
            return;
        }
        //网络波动服务端断开连接
        ctx.executor().schedule(() -> {
            ctx.close();
            client.reConect();
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg != null && msg.equals("close")) {
            log.error(" 服务端关闭连接，客户端也关闭");
            ctx.channel().closeFuture();
            //服务端主动关闭
            close.set(true);
        } else {
            // 往下传
            ctx.fireChannelRead(msg);
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent stateEvent) {
            String eventType = null;
            switch (stateEvent.state()) {
                case READER_IDLE -> eventType = "读空闲";
                case WRITER_IDLE -> eventType = "写空闲";
                case ALL_IDLE -> eventType = "读写空闲";
                default -> eventType = "写空闲";
            }
            log.warn(ctx.channel().remoteAddress() + " 空闲超时事件：" + eventType);

            long data = System.currentTimeMillis();
            log.warn("超时了发送数据start:" + data);
            ctx.pipeline().writeAndFlush(String.valueOf(data));
            log.warn("超时了发送数据end:" + data);
        }
        ctx.fireUserEventTriggered(evt);
    }

}
