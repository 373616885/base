package com.qin.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("==============ExceptionCaughtHandler==============");
        if (cause instanceof BusinessExcetion businessExcetion) {
            businessExcetion.printStackTrace();
        } else {
           log.error(cause.getMessage());
        }
        //Tail释放
        ReferenceCountUtil.release(cause);
        ctx.close();
    }
}
