package com.qin.netty.simple;

import com.qin.netty.encode.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class SimpleEncodeHandler extends MessageToByteEncoder<User> {
    /**
     * 4        4       ?
     * length   age     name
     */
    //一个简单额长度域实现
    @Override
    protected void encode(ChannelHandlerContext ctx, User user, ByteBuf out) throws Exception {
        var nameByte = user.name().getBytes(StandardCharsets.UTF_8);
        var age = user.age();
        var ageLength = 4;
        var nameLength = nameByte.length;
        // 长度域= ageLength + nameLength
        out.writeInt(ageLength + nameLength);
        out.writeInt(age);
        out.writeBytes(nameByte);
    }
}
