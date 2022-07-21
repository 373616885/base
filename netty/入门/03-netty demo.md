### 服务端

```java

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;

public class Server {

    public static void main(String[] args) throws InterruptedException {
        
         /*步骤
         * 创建一个ServerBootstrap b实例用来配置启动服务器
         * b.group指定NioEventLoopGroup来接收处理新连接
         * b.channel指定通道类型
         * b.option设置一些参数
         * b.handler设置日志记录
         * b.childHandler指定连接请求，后续调用的channelHandler
         * b.bind设置绑定的端口
         * b.sync阻塞直至启动服务
        */
        
        // NioEventLoopGroup是一个线程池
        // bossGroup是否可以指定多个呢，可以是可以，但是没有用， 即使指定100，netty最终也只会创建1个。
        // 处理连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("bossGroup"));
        // 处理处理Channel（通道）的I/O事件。
        EventLoopGroup workGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("bossGroup"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup);
        // 指明使用NIO进行网络通讯
        serverBootstrap.channel(NioServerSocketChannel.class);
        // server 启动过程中的一段逻辑
        //serverBootstrap.handler(new LoggingHandler());
        serverBootstrap.handler(new ServerHandler());
        // 设置TCP连接个数--完成握手和没完成握手队列之和 ，默认是50
        serverBootstrap.option(ChannelOption.SO_BACKLOG,128);
        // 给每条连接设置TCP属性
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
         // 给每条连接设置TCP属性
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        // 自定义基本属性
        serverBootstrap.childAttr(AttributeKey.newInstance("childAttr"), "qjp");

        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest httpRequest) {
                        byte[] content = "<h1>hello world</h1>".getBytes();
                        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                        response.content().writeBytes(content);
                        response.headers().set("Content-Length", content.length);
                        channelHandlerContext.writeAndFlush(response);
                    }
                });
            }
        });
        ChannelFuture channelFuture = serverBootstrap.bind(8085).sync();
        ChannelFuture closeFuture = channelFuture.channel().closeFuture();
        closeFuture.sync();

        // finally
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
    
   
    
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelRegistered");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded");
    }
}
    
```





**bossGroup：用于接收客户端的链接**

接收客户端TCP链接，初始化Channel参数

将链路状态变更时间通知给ChannelPipeline

**workGroup：用于处理I/O相关的读写操作，或者执行系统Task、定时任务Task等**

异步读取通信对端的数据报，发送读时间到ChannelPipeline

异步发送消息到通信对端，调用channelPipeline的消息发送接口

执行系统调用task

执行定式任务Task，例如链路空闲状态监测定式任务。





### 服务端配置步骤

```java
* 创建一个ServerBootstrap b实例用来配置启动服务器
* b.group指定NioEventLoopGroup来接收处理新连接
* b.channel指定通道类型
* b.option设置一些参数
* b.handler设置日志记录
* b.childHandler指定连接请求，后续调用的channelHandler
* b.bind设置绑定的端口
* b.sync阻塞直至启动服务
```


### 测试

```java
w10
1、点击开始菜单，输入“控制面板”，点击进入
2、在控制面板中，点击 程序 — 启动或关闭windows功能。
3、然后勾选Telnet客户端选项，确定进行安装。稍等片刻，确定即可。
    
telnet 127.0.0.1 8085    
```







​	