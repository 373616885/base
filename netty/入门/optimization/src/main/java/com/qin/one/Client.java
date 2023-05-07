package com.qin.one;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Client {

    public static void main(String[] args) throws InterruptedException {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        //客户端
        var bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(EmptyHandler.INSTANCE);


        //发起异步连接操作，同步阻等待结果
        var port = 8000;
        while (!Thread.interrupted()) {
            bootstrap.connect("127.0.0.1", port++)
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            log.warn("connect failed exit!");
                            System.exit(0);
                        }
                    }).sync();
            if (port > 8099) {
                port = 8000;
            }
        }


    }


    @ChannelHandler.Sharable
    private static class EmptyHandler extends ChannelInboundHandlerAdapter {

        static final EmptyHandler INSTANCE = new EmptyHandler();

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.warn("connect success :" + ctx.channel().remoteAddress());
        }
    }


}
