package com.qin.heart;

import cn.hutool.json.JSONUtil;
import com.qin.netty.encode.Admin;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBizServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.warn("读到客户端发送的数据:" + msg);
        Admin admin = new Admin(32, "HeartBizServerHandler-qinjp");
        final var parse = JSONUtil.toJsonStr(admin);
        log.warn("向客户端发送的数据:" + parse);
        ctx.pipeline().writeAndFlush(parse);
    }
}
