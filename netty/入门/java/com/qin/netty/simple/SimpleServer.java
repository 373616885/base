package com.qin.netty.simple;

import com.qin.netty.ExceptionCaughtHandler;
import com.qin.netty.ServerHandler;
import com.qin.netty.encode.BizHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleServer {

    public static void main(String[] args) {
        log.debug("Log4J2LoggerFactory.INSTANCE");
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        // NioEventLoopGroup是一个线程池
        // bossGroup是否可以指定多个呢，可以是可以，但是没有用， 即使指定100，netty最终也只会创建1个。
        // 处理连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("bossGroup"));
        // 处理处理Channel（通道）的I/O事件。要是 2的次数幂，才是最优解
        EventLoopGroup workGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("workGroup"));

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workGroup);
            // 指明使用NIO进行网络通讯
            serverBootstrap.channel(NioServerSocketChannel.class);
            // server 启动过程中的一段逻辑
            //serverBootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
            serverBootstrap.handler(new ServerHandler());
            // 设置TCP连接个数--完成握手和没完成握手队列之和 ，默认是50
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
            // 给 NioServerSocketChannel自定义一些属性 通过channel.attr() 取出这个属性
            serverBootstrap.attr(AttributeKey.newInstance("serverName"), "nettyServer");
            // 这两个一般用于客户端
            // 给每条连接设置TCP属性
            // 表示是否开启 TCP 底层心跳机制，true 为开启
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            // 表示是否开启 Nagle 算法，true 表示关闭，false 表示开启，
            // 通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            // 给每条连接自定义基本属性 可以通过 channel.attr() 取出该属性
            // 常见的运用场景，客户端登录成功之后，给其对应的 Channel 绑定标识，下次只需要判断该 Channel 是否有标识即可知道其是否已经登录
            serverBootstrap.childAttr(AttributeKey.newInstance("token"), "373616885");

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new SimpleEncodeHandler());
                    ch.pipeline().addLast(new SimpleBizHandler());
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // finally
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}
