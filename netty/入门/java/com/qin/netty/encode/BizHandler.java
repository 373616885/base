package com.qin.netty.encode;

import cn.hutool.json.JSONUtil;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BizHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        if (msg instanceof AbstractByteBuf byteBuf && byteBuf.isReadable()) {
            log.warn("读到客户端发送的数据:" + byteBuf.toString(CharsetUtil.UTF_8));
        }
        Admin admin = new Admin(32, "qinjp");
        final var parse = JSONUtil.toJsonStr(admin);
        log.warn("向客户端发送的数据:" + parse);
        ctx.pipeline().writeAndFlush(parse);
    }

}
