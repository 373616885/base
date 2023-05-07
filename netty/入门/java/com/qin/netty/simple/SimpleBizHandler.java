package com.qin.netty.simple;

import com.qin.netty.encode.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleBizHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        User user = new User(32, "qinjp");
        ctx.pipeline().writeAndFlush(user);
    }
}
