### netty 是什么？

netty是一个框架，对NIO的封装



### demo

```java
package com.qin.netty.auth;

import com.qin.netty.ExceptionCaughtHandler;
import com.qin.netty.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class AuthServer {
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
                    // 解码成HttpRequest
                    ch.pipeline().addLast(new HttpServerCodec());
                    // 解码成FullHttpRequest,解析post的boby
                    // 其实可以优化，当是get请求时，可以不加这个处理器
                    ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));

                    //ch.pipeline().addLast(new AuthServerHandler());

                    ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest httpRequest) {
                            System.out.println("httpRequest : " + httpRequest.toString());
                            ByteBuf buf = httpRequest.content();
                            System.out.println("httpRequest content: " + buf.toString(StandardCharsets.UTF_8));

                            Attribute<String> tokenAttr = ch.attr(AttributeKey.valueOf("token"));
                            String token = tokenAttr.get();
                            Attribute<String> serverNameAttr = ch.parent().attr(AttributeKey.valueOf("serverName"));
                            String serverName = serverNameAttr.get();
                            String msg = """
                                    <h1>serverName: %s </h1>
                                    <h1>token: %s</h1>   
                                    """.formatted(serverName, token);
                            log.warn(msg);
                            byte[] content = msg.getBytes();
                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                            response.content().writeBytes(content);
                            response.headers().set("Content-Length", content.length);
                            channelHandlerContext.writeAndFlush(response);
                        }


                    });
                    ch.pipeline().addLast(new ExceptionCaughtHandler());
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
```



### 工作流程



### new 2个线程池，bossGroup和workGroup

```java
// NioEventLoopGroup是一个线程池
// bossGroup是否可以指定多个呢，可以是可以，但是没有用， 即使指定100，netty最终也只会创建1个。
// 处理连接
EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("bossGroup"));
// 处理处理Channel（通道）的I/O事件。要是 2的次数幂，才是最优解
EventLoopGroup workGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("workGroup"));
```

主要事情：

创建线程执行器 executor

给NioEventLoopGroup创建子NioEventLoop

创建线程选择器chooserFactory.newChooser(children)

```java
//创建线程执行器
executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
//给NioEventLoopGroup创建子NioEventLoop
children = new EventExecutor[nThreads];
children[i] = newChild(executor, args);
//创建线程选择器--next（加一取模拿下一个）
chooser = chooserFactory.newChooser(children);
```

其中最主要的newChild

保存线程执行器 ThreadPerTaskExecutor：Group传过来的负责创建一个新线程去执行任务

创建一个newMpscQueue（ 任务队列-外部线程也可以将任务扔进来）

创建一个Selector 多路复用器 : 每一个NioEventLoop都有一个自己的Selector



### 接着启动服务端

1）创建服务端Channel

​	将 Java NIO 底层的Channel封装

2）初始化服务端Channel

​	初始化一些基本属性，以及添加逻辑处理器 （处理新连接的特殊逻辑处理器）

3）注册selector

​	netty将JDK底层的ServerSocketChannel注册到 selector上

​	并将服务端netty封装的Channel 作为 attachment  绑定到ServerSocketChannel上

4)  端口绑定

​	最后对Java底层端口的绑定，修改事件为accept



**整体代码流程**

bind()  代码入口

​	initAndRegister()  初始化并注册

​		hannelFactory.newChannel() 创建netty服务端Channel

​	    init(channel) 初始化服务端Channel

​		config().group().register(channel) 拿到bossGroup.NioEventLoop这个线程去注册

 	   		channel.eventLoop().execute() 用bossGroup.NioEventLoop这个线程去执行NioEventLoop.run，下面的任务，到此main方法任务基本结束

​				下面的任务都被封装成一个task，等待NioEventLoop.run去执行

​				doBind0 

​						channel.bind()  channel绑定IP和端口

​							pipeline.fireChannelActive(); 绑定完成后，调用channelActive 传播事件

​									readIfIsAutoRead -> unsafe.beginRead -> doBeginRead 注册关心的selectionKey.OP_ACCEPT



到此服务端完成了

Channel创建，初始化（给Channel添加特殊的ServerBootstrapAcceptor），注册

并分配了一个bossGroup.NioEventLoop线程去 -- 绑定IP和端口，注册selectionKey.OP_ACCEPT

到此main方法任务基本结束

接着就是bossGroup.NioEventLoop.run服务端的处理





### 服务端处理

bossGroup.NioEventLoop.run

1. select() 检查注册到Selector上的Channel是否有IO事件
2. processSelectedKeys() 处理新连接接人
3. runAllTasks() 处理异步任务队列 --处理队列和处理IO事件的时间个50%



bossGroup.NioEventLoop这个线程会阻塞在当前线程的selector.select()（没一个线程一个selector）

当有新连接接人就到

processSelectedKeys 处理新连接接人



最终去执行：**NioMessageUnsafe.read()**

调用java底层channel.accept得到SocketChannel，将其封装成NioSocketChannel

调用channelRead事件传播，这个事件调用



NioServerSocketChannel特有的节点ServerBootstrapAcceptor.channelRead

ServerBootstrapAcceptor的channelRead主要三件事

1. 给NioSocketChannel添加用户自定义的childHandler
2. 给NioSocketChannel设置option和attr
3. 调用NioSocketChannel注册--给NioSocketChannel分配一个workGroup.NioEventLoop，同时在对应线程上的selector注册
4. 最后selector上感兴趣的事件改为read

```java
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    final Channel child = (Channel) msg;
	//给NioSocketChannel添加用户自定义的childHandler
    child.pipeline().addLast(childHandler);
	//给NioSocketChannel设置option
    setChannelOptions(child, childOptions, logger);
    //给NioSocketChannel设置attr
    setAttributes(child, childAttrs);

    try {
        //向NioSocketChannel的NioEventLoop的selector上注册，完成后注册读事件
        childGroup.register(child).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    forceClose(child, future.cause());
                }
            }
        });
    } catch (Throwable t) {
        forceClose(child, t);
    }
}
```



到此处理IO事件结束



处理异步任务队列  runAllTasks()



接着循环回到当前线程自己的selector的select方法





### 客户端处理

上面说道服务端给NioSocketChannel分配了一个workGroup.NioEventLoop，同时在selector上完成了注册读事件

分配NioEventLoop，最终也是调用 workGroup.NioEventLoop.run

整体步骤

1. select() 检查Channel的是否有读事件
2. processSelectedKeys() 处理读事件
3. runAllTasks() 处理异步任务队列



workGroup.NioEventLoop这个线程会阻塞在当前线程的selector.select()（没一个线程一个selector）

当客户点给NioSocketChannel发送信息就到

processSelectedKeys() 处理读事件



最终去执行：**NioByteUnsafe.read()**

分配内存

最终调用Java底层读到分配的内存中

调用channelRead事件传播，

接着调用用户自定义的childHandler



调用完成之后回到当前线程自己的selector的select方法



























