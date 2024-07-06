package com.qin.two;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Administrator
 */
@Slf4j
public class Server {

    @SneakyThrows
    public static void main(String[] args) {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        // NioEventLoopGroup是一个线程池
        // bossGroup是否可以指定多个呢，可以是可以，但是没有用， 即使指定100，netty最终也只会创建1个。
        // 处理连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("bossGroup"));
        // 处理处理Channel（通道）的I/O事件。要是 2的次数幂，才是最优解
        EventLoopGroup workGroup = new NioEventLoopGroup(new DefaultThreadFactory("workGroup"));

        EventLoopGroup bizGroup = new NioEventLoopGroup(30, new DefaultThreadFactory("bizGroup"));

        var serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workGroup);
        // 指明使用NIO进行网络通讯
        serverBootstrap.channel(NioServerSocketChannel.class);
        // 设置TCP连接个数--完成握手和没完成握手队列之和 ，默认是50
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);

        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                ch.pipeline().addLast(ServerBizHandler.INSTANCE);
                ch.pipeline().addLast(bizGroup, ServerBizHandler.INSTANCE);
            }
        });


        var channelFuture = serverBootstrap.bind(8080).addListener(future -> {
            if (future.isSuccess()) {
                log.warn("server start success");
            }
        }).sync();//等待bind完成

        //阻塞在此
        channelFuture.channel().closeFuture().sync();

        // finally
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();

    }


    @ChannelHandler.Sharable
    private static class ServerBizHandler extends SimpleChannelInboundHandler<ByteBuf> {

        static final ServerBizHandler INSTANCE = new ServerBizHandler();

        private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(30, new DefaultThreadFactory("bizGroup"));

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            // 业务转换ByteBuf
            EXECUTOR.submit(() -> {
                msg.retain();
                getResult();
                ctx.writeAndFlush(msg);
            });
        }

        @SneakyThrows
        private void getResult() {
            // 90% 1ms
            // 95 % 10ms
            // 99% 100ms
            // 99.99% 1000ms

            var level = ThreadLocalRandom.current().nextInt(1000);

            var time = 1000;

            if (level <= 900) {
                time = 50;
            } else if (level <= 950) {
                time = 100;
            } else if (level <= 990) {
                time = 500;
            }


            Thread.sleep(time);
        }
    }


}
