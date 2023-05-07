package com.qin.netty.bound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundHandlerB extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("InboundHandlerB: " + msg);
        ctx.fireChannelRead(msg);
    }

    // 客户端可用的时候传递一个channelRead
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("hello InboundHandlerB.qinjp");
        ctx.fireChannelRead("hello InboundHandlerB.qinjp");
    }
}
