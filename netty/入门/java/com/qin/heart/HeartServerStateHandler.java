package com.qin.heart;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartServerStateHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent stateEvent) {
            String eventType = null;
            switch (stateEvent.state()) {
                case READER_IDLE -> eventType = "读空闲";
                case WRITER_IDLE -> eventType = "写空闲";
                case ALL_IDLE -> eventType = "读写空闲";
                default -> eventType = "读空闲";
            }
            log.warn(ctx.channel().remoteAddress() + " 空闲超时事件：" + eventType);
            ctx.pipeline().writeAndFlush("close");
            ctx.pipeline().close();
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
