package com.qin.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class SimpleDecodeHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof ByteBuf byteBuf) {
            final int length = byteBuf.readInt();
            System.out.println("length:" + length);
            final int age =  byteBuf.readInt();
            System.out.println("age:" + age);
            final String name = new String(ByteBufUtil.getBytes(byteBuf));
            System.out.println("name:" + name);
            System.out.println("name:" + byteBuf.toString(StandardCharsets.UTF_8));
        }
    }
}
