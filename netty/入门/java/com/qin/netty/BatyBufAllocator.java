package com.qin.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.concurrent.FastThreadLocalThread;

import java.nio.ByteOrder;

public class BatyBufAllocator {

    public static void main(String[] args) {
        PooledByteBufAllocator aDefault = PooledByteBufAllocator.DEFAULT;

        ByteBuf byteBuf = aDefault.directBuffer(16);
        String msg = "qinjp";
        byteBuf.writeBytes(msg.getBytes());



        byteBuf.order(ByteOrder.BIG_ENDIAN).writeInt(4);

        byteBuf.release();

    }
}
