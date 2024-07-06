package com.qin.netty.client;

import com.qin.netty.ExceptionCaughtHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {
    public static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    public static void main(String[] args) {
        //客户端
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //socketChannel.pipeline().addFirst(new IdleStateHandler(3, 0, 0));
                        //ByteBuff的将长度域去掉
                        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(2048, 0, 4, 0, 4));
                        //ByteBuff转成String
                        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                        //String = idle close 就断线重连
                        //socketChannel.pipeline().addLast(new HeartBeatClientHandler());
                        //拿到String业务处理
                        socketChannel.pipeline().addLast(new ClientHandler());
                        //异常的处理
                        socketChannel.pipeline().addLast(new ExceptionCaughtHandler());
                    }
                });
        connect(bootstrap);
    }

    public static void connect(Bootstrap bootstrap) {
        log.debug("Log4J2LoggerFactory.INSTANCE");
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        try {
            //发起异步连接操作，同步阻等待结果
            ChannelFuture channelFuture = bootstrap
                    .connect("127.0.0.1", 8080)
                    //启动时如果连接失败，会断线重连
                    .addListener(new ConnectionListener(bootstrap))
                    .sync();
            //等待客户端链路关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放NIO线程组 不能马上释放需要
            //eventLoopGroup.shutdownGracefully();
        }

    }
}
