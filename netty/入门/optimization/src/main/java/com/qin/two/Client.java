package com.qin.two;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Client {

    public static void main(String[] args) throws InterruptedException {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);

        //客户端
        var bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                        ch.pipeline().addLast(ClientBizHandler.INSTANCE);
                    }
                });

        // 1000个连接每个连接1秒发送一个请求
        for (int i = 0; i < 1000; i++) {
            bootstrap.connect("127.0.0.1", 8080)
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            log.warn("connect failed exit!");
                            System.exit(0);
                        }
                    }).sync();
        }

    }


    @ChannelHandler.Sharable
    static class ClientBizHandler extends SimpleChannelInboundHandler<ByteBuf> {

        static final ClientBizHandler INSTANCE = new ClientBizHandler();

        private static final AtomicLong TOTAL_RESPONSE_TIME = new AtomicLong();
        private static final AtomicLong TOTAL_RESPONSE_COUNT = new AtomicLong();
        private static final AtomicLong START_TIME = new AtomicLong();

        private static final FastThreadLocalThread THREAD = new FastThreadLocalThread(() -> {
            while (true) {
                long duration = System.currentTimeMillis() - START_TIME.get();
                if (duration > 0) {
                    long count = TOTAL_RESPONSE_COUNT.get();
                    final long total_time = TOTAL_RESPONSE_TIME.get();
                    float qps = 1000 * count / duration;
                    float avgTime = total_time / count;
                    System.out.println("qps:" + qps + " avg:" + avgTime + " count:" + count + " total_time:" + total_time);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "qps");


        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            log.warn("connect success :" + ctx.channel().remoteAddress());
            var byteBuf = Unpooled.directBuffer(Long.BYTES);//重用非池的;
            ctx.executor().scheduleWithFixedDelay(() -> {
                byteBuf.retain();
                var data = System.currentTimeMillis();
                byteBuf.writeLong(data);
                ctx.writeAndFlush(byteBuf);
                byteBuf.clear();
            }, 1, 2, TimeUnit.SECONDS);
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            TOTAL_RESPONSE_TIME.addAndGet(System.currentTimeMillis() - msg.readLong());
            TOTAL_RESPONSE_COUNT.incrementAndGet();
            if (START_TIME.compareAndSet(0, System.currentTimeMillis())) {
                THREAD.start();
            }
        }
    }


}
