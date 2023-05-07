package com.qin.one;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        EventLoopGroup workGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("workGroup"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workGroup);
        // 指明使用NIO进行网络通讯
        serverBootstrap.channel(NioServerSocketChannel.class);
        // 设置TCP连接个数--完成握手和没完成握手队列之和 ，默认是50
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);

        serverBootstrap.childHandler(ConnectCountHandler.INSTANCE);


        for (var i = 0; i < 100; i++) {
            final int port = 8000 + i;
            serverBootstrap.bind(port).addListener(future -> {
                if (future.isSuccess()) {
                    log.warn("server start success port:" + port);
                }
            }).sync();//等待bind完成
        }

        // finally
        //bossGroup.shutdownGracefully();
        //workGroup.shutdownGracefully();
        // 服务端不需要向客户端一样，main方法线程可以结束
        // 客户端线程main线程结束了，connect也就结束了

    }

    private static final AtomicInteger COUNT = new AtomicInteger();

    @ChannelHandler.Sharable
    private static class ConnectCountHandler extends ChannelInboundHandlerAdapter {

        private ConnectCountHandler() {
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                log.warn("connects: " + COUNT.get());
            }, 1, 2, TimeUnit.SECONDS);
        }

        static final ConnectCountHandler INSTANCE = new ConnectCountHandler();

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            COUNT.incrementAndGet();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //log.warn("异常终止: " + cause.getMessage());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            COUNT.decrementAndGet();
        }
    }


}
