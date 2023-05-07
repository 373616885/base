package com.qin.netty.bound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.TimeUnit;

public class InboundHandler0 extends ChannelInboundHandlerAdapter {

    // 客户端可用的时候传递一个channelRead
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //从head开始往下传
        //ctx.pipeline().fireChannelRead("hello InboundHandler0.qinjp");
        System.out.println("InboundHandler0");
        //从当前节点接着往下传
        ctx.fireChannelActive();

        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                ctx.pipeline().write("hello qinjp");
            }
        },3, TimeUnit.SECONDS);
    }

}