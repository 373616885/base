package com.qin.netty.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.UnsupportedEncodingException;

public class AuthServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.pipeline().remove(this);
    }


    private boolean pass(ByteBuf buf) throws UnsupportedEncodingException {
        //增加引用计数
        //ByteBuf使用了引用计数，缺省下读取一次之后refCnt就会减到0，再读就出现异常了。
        //如果使用retain()可以增加引用计数，可以多读一次；相反，release()则减一次，可能还没有读就不能再读了
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req,"UTF-8");


        //byte[] bytes = ByteBufUtil.getBytes(buf);

        buf.retain();
        return true;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
