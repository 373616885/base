package com.qin.heart;

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
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Administrator
 */
@Slf4j
public class HeartClient {

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private String ip = "127.0.0.1";
    private int port = 8080;

    public void beginConnect() {
        try {
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            assembleBootStrap();
            //这里会阻塞
            connect();
        } catch (Exception e) {
            log.error("客户端连接发送错误");
            e.printStackTrace();
            shutDown();
        }
    }

    void connect() throws InterruptedException {
        log.info("连接开始，channel信息为：{}:{}", ip, port);
        //同步
        ChannelFuture channelFuture = bootstrap.connect(ip, port).addListener(new ReConnectionListener(this));
        //main方法会阻塞在这
        channelFuture.channel().closeFuture().sync();
    }

    private static final AtomicInteger reconnectNum = new AtomicInteger(0);

    void reConect() {
        if (reconnectNum.incrementAndGet() > 5) {
            log.error("重连次数过多");
            //释放NIO线程组
            this.shutDown();
            return;
        }

        try {
            //重连
            this.connect();
        } catch (Exception e) {
            log.error("重连失败");
        }
    }

    void shutDown() {
        bootstrap = null;
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        eventLoopGroup = null;
        log.error("客户端关闭连接，channel信息为：{} : {}", ip, port);
    }

    private void assembleBootStrap() {
        log.debug("Log4J2LoggerFactory.INSTANCE");
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        HeartClient client = this;
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //客户端写空闲了，就ping一下
                        socketChannel.pipeline().addFirst(new IdleStateHandler(0, 4, 0));

                        //ByteBuff的将长度域去掉
                        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(2048, 0, 4, 0, 4));
                        //ByteBuff转成String
                        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));

                        //String = idle close 就断线重连
                        socketChannel.pipeline().addLast(new HeartClientStateHandler(client));

                        //将ByteBuf转成有长度域的ByteBuf
                        //会分两次write,一次写 length ,一次写数据
                        socketChannel.pipeline().addLast(new LengthFieldPrepender(4));
                        //将String转成ByteBuf
                        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));

                        //拿到String业务处理
                        socketChannel.pipeline().addLast(new HeartBizClientHandler());
                        //异常的处理
                        socketChannel.pipeline().addLast(new ExceptionCaughtHandler());
                    }
                });
    }


    public static void main(String[] args) {
        HeartClient client = new HeartClient();
        client.beginConnect();

    }

}
