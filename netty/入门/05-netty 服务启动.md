### 服务端demo

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
        EventLoopGroup workGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("workGroup"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup);
        // 指明使用NIO进行网络通讯
        serverBootstrap.channel(NioServerSocketChannel.class);
        // server 启动过程中的一段逻辑
        //serverBootstrap.handler(new LoggingHandler());
        serverBootstrap.handler(new ServerHandler());
        // 设置TCP连接个数--完成握手和没完成握手队列之和 ，默认是50
        serverBootstrap.option(ChannelOption.SO_BACKLOG,128);
        // 给 NioServerSocketChannel自定义一些属性 通过channel.attr() 取出这个属性
        serverBootstrap.attr(AttributeKey.newInstance("serverName"), "nettyServer");
        // 这两个一般用于客户端
        // 给每条连接设置TCP属性
        // 表示是否开启 TCP 底层心跳机制，true 为开启
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        // 表示是否开启 Nagle 算法，true 表示关闭，false 表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        // 给每条连接自定义基本属性 可以通过 channel.attr() 取出该属性
        // 常见的运用场景，客户端登录成功之后，给其对应的 Channel 绑定标识，下次只需要判断该 Channel 是否有标识即可知道其是否已经登录
        serverBootstrap.childAttr(AttributeKey.newInstance("token"), "373616885");

        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {

                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest httpRequest) {
                        Attribute<String> tokenAttr = ch.attr(AttributeKey.valueOf("token"));
                        String token = tokenAttr.get();
                        Attribute<String> serverNameAttr = ch.parent().attr(AttributeKey.valueOf("serverName"));
                        String serverName = serverNameAttr.get();
                        String msg = """
                                <h1>serverName: %s </h1>
                                <h1>token: %s</h1>   
                                """.formatted(serverName, token);
                        byte[] content = msg.getBytes();
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



### 开始疑惑的两个问题

服务端的socket在哪里是初始化？

在哪里accept连接？



### 服务端启动

1）创建服务端Channel

​	将 Java NIO 底层的Channel封装

2）初始化服务端Channel

​	初始化一些基本属性，以及添加逻辑处理器 （处理新连接的特殊逻辑处理器）

3）注册selector

​	netty将JDK底层的ServerSocketChannel注册到 selector上

​	并将服务端netty封装的Channel 作为 attachment  绑定到ServerSocketChannel上

4)  端口绑定

​	最后对Java底层端口的绑定，修改事件为accept



#### 整体代码流程

bind()  代码入口

​	initAndRegister()  初始化并注册

​		hannelFactory.newChannel() 创建netty服务端Channel

​	    init(channel) 初始化服务端Channel

​		config().group().register(channel) 注册

​	doBind0 绑定IP和端口

​		channel.bind()  channel绑定IP和端口

​			pipeline.fireChannelActive(); 绑定完成后，调用channelActive 传播事件

​					readIfIsAutoRead -> unsafe.beginRead -> doBeginRead 注册关心的selectionKey.OP_ACCEPT



### 创建服务端Channel

bind()  代码入口

​	initAndRegister()  初始化并注册

​		hannelFactory.newChannel() 创建netty服务端Channel

​	    init(channel) 初始化服务端Channel

```java
final ChannelFuture initAndRegister() {
    .......    
    // 创建  服务端Channel  
    channel = channelFactory.newChannel();
    // 初始化服务端Channel
    init(channel);
    ......
}     

channelFactory 的来历   
serverBootstrap.channel(NioServerSocketChannel.class);

//NioServerSocketChannel传递到ReflectiveChannelFactory里
public B channel(Class<? extends C> channelClass) {
    return channelFactory(new ReflectiveChannelFactory<C>(
        ObjectUtil.checkNotNull(channelClass, "channelClass")
    ));
}

ReflectiveChannelFactory
@Override
public T newChannel() {
	.......    
    // JDK底层的反射NioServerSocketChannel的构造器去创建对象    
    return constructor.newInstance();
}

初始化服务端Channel
@Override
void init(Channel channel) {
    setChannelOptions(channel, newOptionsArray(), logger);
    setAttributes(channel, newAttributesArray());

    ChannelPipeline p = channel.pipeline();

    final EventLoopGroup currentChildGroup = childGroup;
    final ChannelHandler currentChildHandler = childHandler;
    final Entry<ChannelOption<?>, Object>[] currentChildOptions = newOptionsArray(childOptions);
    final Entry<AttributeKey<?>, Object>[] currentChildAttrs = newAttributesArray(childAttrs);

    p.addLast(new ChannelInitializer<Channel>() {
        @Override
        public void initChannel(final Channel ch) {
            final ChannelPipeline pipeline = ch.pipeline();
            ChannelHandler handler = config.handler();
            if (handler != null) {
                pipeline.addLast(handler);
            }

            ch.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    pipeline.addLast(new ServerBootstrapAcceptor(
                        ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                }
            });
        }
    });
}

```





### hannelFactory.newChannel() 创建netty服务端Channel

整个过程就是NioServerSocketChannel 反射构造器的过程

1. newChannel()    反射创建 JDK 底层Chanel
2. new NioServerSocketChannelConfig  TCP参数配置 
3. AbstractNioChannel()  
   1. AbstractChannel()  创建ID，unsafe，pipeline
   2. ch.configureBlocking(false); 



#### newChannel() 反射创建 JDK 底层Chanel

通过 SelectorProvider反射openServerSocketChannel方法去创建jdk底层Channel

```java
	// 通过 SelectorProvider反射openServerSocketChannel去创建
	private static ServerSocketChannel newChannel(SelectorProvider provider, InternetProtocolFamily family) {
        try {
            ServerSocketChannel channel =
                    SelectorProviderUtil.newChannel(OPEN_SERVER_SOCKET_CHANNEL_WITH_FAMILY, provider, family);
            return channel == null ? provider.openServerSocketChannel() : channel;
        } catch (IOException e) {
            throw new ChannelException("Failed to open a socket.", e);
        }
    }
```



#### new NioServerSocketChannelConfig

new NioServerSocketChannelConfig 通过这个类去配置各种参数例如TCP参数

```java
public NioServerSocketChannel(ServerSocketChannel channel) {
    super(null, channel, SelectionKey.OP_ACCEPT);
    config = new NioServerSocketChannelConfig(this, javaChannel().socket());
}
```



#### AbstractNioChannel()  

ch.configureBlocking(false);

```java
	protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
        // 这里创建3个属性 创建ID，unsafe，pipeline
        super(parent);
        this.ch = ch;
        this.readInterestOp = readInterestOp;
        try {
            ch.configureBlocking(false);
        } catch (IOException e) {
            try {
                ch.close();
            } catch (IOException e2) {
                logger.warn(
                            "Failed to close a partially initialized socket.", e2);
            }

            throw new ChannelException("Failed to enter non-blocking mode.", e);
        }
    }
```

AbstractChannel()  创建ID，unsafe，pipeline

```java
	protected AbstractChannel(Channel parent) {
        this.parent = parent;
        id = newId();
        unsafe = newUnsafe();
        pipeline = newChannelPipeline();
    }
```





### 初始化服务端Channel

**init(channel) 初始化服务端Channel**

1. set  ChannelOptions  和 ChannelAttributes
2. set ChildOptions 和 ChildAttrs
3. config handler 将ChannelHandler配置到pipeline中（用户自定义配置到pipeline中）
4. add ServerBootstrapAcceptor 服务端添加一个特殊的处理器（acceptor的时候特殊Handler）

```java
@Override
void init(Channel channel) {
    //set  ChannelOptions 给 channel配置 用户配置的属性 option 
    //例如：serverBootstrap.option(ChannelOption.SO_BACKLOG,128);
    setChannelOptions(channel, newOptionsArray(), logger);
    //set  ChannelAttributes  给 channel配置用户自定义的属性
    //例如：serverBootstrap.attr(AttributeKey.newInstance("serverName"), "nettyServer");
    setAttributes(channel, newAttributesArray());

    ChannelPipeline p = channel.pipeline();

    final EventLoopGroup currentChildGroup = childGroup;
    final ChannelHandler currentChildHandler = childHandler;
    final Entry<ChannelOption<?>, Object>[] currentChildOptions = newOptionsArray(childOptions);
    final Entry<AttributeKey<?>, Object>[] currentChildAttrs = newAttributesArray(childAttrs);
	
    p.addLast(new ChannelInitializer<Channel>() {
        @Override
        public void initChannel(final Channel ch) {
            final ChannelPipeline pipeline = ch.pipeline();
            // config handler 拿到用户自定义的ChannelHandler
            ChannelHandler handler = config.handler();
            if (handler != null) {
                // 配置到pipeline中
                pipeline.addLast(handler);
            }

            ch.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    // 添加一个特殊的处理器
                    pipeline.addLast(new ServerBootstrapAcceptor(
                        ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                }
            });
        }
    });
}   
```

总结：保存用户的自定义属性和handler 通过这些属性添加到一个特殊的处理器

这个特殊的处理器对acceptor新连接进行特殊Handler





### 注册selector

在 newChannel() 和 init(channel)  之后

就进行注册多路复用器 selector

具体代码： AbstractChannel.AbstractUnsafe.register()

调用过程：

AbstractChannel.AbstractUnsafe.register()  入口

​	AbstractChannel.this.eventLoop = eventLoop; 绑定线程

​	register0(promise); 实际注册

​			doRegister(); 调用java底层注册

​						Channel注册Selector： 

​								 0  对事件不感兴趣 --ChannelActive事件修改selectionKey 为accept事件

​								 this  将NioServerSocketChannel作为attachment附件对象

​						selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);

​			pipeline.invokeHandlerAddedIfNeeded(); 触发Handler添加事件 

​			pipeline.fireChannelRegistered();  触发事件Channel注册事件

​			if (isActive()) 就触发ChannelActive事件修改selectionKey 为accept事件 

​						这因为没有完成端口绑定所以是false ，一般不会触发



**绑定端口完成后触发ChannelPipeline中的ChannelActive事件开始传播，最终会调用AbstractNioUnsafe#beginRead()方法**
**修改 selectionKey selectionKey 为accept事件**



```java
    final ChannelFuture initAndRegister() {
        Channel channel = null;
        try {
            // newChannel() 反射创建 JDK 底层Chanel
            channel = channelFactory.newChannel();
            // init(channel) 初始化服务端Channel
            init(channel);
        } catch (Throwable t) {
            if (channel != null) {
                // channel can be null if newChannel crashed (eg SocketException("too many open files"))
                channel.unsafe().closeForcibly();
                // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
                return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
            }
            // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
            return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
        }
		// 入口：注册多路复用器 selector
        ChannelFuture regFuture = config().group().register(channel);
        .......
    }


/* 
    config().group().register(channel);
    调用链：  
    MultithreadEventLoopGroup ——》
    SingleThreadEventLoop --》
    AbstractChannel --》
    // unsafe()会返回一个NioServerSocketChannel初始化的时候new NioMessageUnsafe实例 
    promise.channel().unsafe().register(this, promise);-->
    // 调用 NioServerSocketChannel.AbstractChannel.AbstractUnsafe.register()
    NioMessageUnsafe.register(this, promise);
    NioServerSocketChannel.AbstractChannel.AbstractUnsafe.register()
    // 调用doRegister();
    这个方法不在内部类NioMessageUnsafe中，而是在 NioServerSocketChannel.AbstractChannel 类里面
    这时候this对象就是 NioServerSocketChannel 
    参数 0 对如何事件都不感兴趣：这里是通过注册完成后触发事件：修改 selectionKey 为accept事件
    selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
    注册完成后：
    触发ChannelPipeline中的ChannelActive事件开始传播，最终会调用AbstractNioUnsafe#beginRead()方法
    修改 selectionKey selectionKey 为accept事件
    
*/

//SingleThreadEventLoop 类
@Override
public ChannelFuture register(Channel channel) {
    // channel 就是 NioServerSocketChannel
    return register(new DefaultChannelPromise(channel, this));
}

@Override
public ChannelFuture register(final ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    // promise.channel()得到 NioServerSocketChannel
    // unsafe() 得到 NioServerSocketChannel初始化的时候new NioMessageUnsafe实例 
    // NioMessageUnsafe.register(this, promise);最终调用
    // NioServerSocketChannel.AbstractChannel.AbstractUnsafe.register()
    promise.channel().unsafe().register(this, promise);
    return promise;
}


// NioServerSocketChannel.AbstractChannel.AbstractUnsafe.register()
@Override
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    .....
    //  绑定线程   
    AbstractChannel.this.eventLoop = eventLoop;

    if (eventLoop.inEventLoop()) {
        // 实际注册
        register0(promise);
    } 
    //  触发Handler添加事件 
    pipeline.invokeHandlerAddedIfNeeded();
    //  触发事件Channel注册事件
    pipeline.fireChannelRegistered();
    // 如果已经注册完成-就触发ChannelActive事件
    // ChannelActive事件里调用AbstractNioUnsafe#beginRead()方法修改Channel关心事件
    if (isActive()) {
        if (firstRegistration) {
            pipeline.fireChannelActive();
        } else if (config().isAutoRead()) {
            // beginRead()方法修改Channel关心事件
            beginRead();
        }
    }
    ......
}

private void register0(ChannelPromise promise) {
    try {
        
        // 调用java底层注册
        // 调用doRegister();
        // 这个方法不在内部类NioMessageUnsafe中，而是在 NioServerSocketChannel.AbstractChannel 类里面
        // 这时候里面this对象就是 NioServerSocketChannel 
        doRegister();
        neverRegistered = false;
        registered = true;

        // Ensure we call handlerAdded(...) before we actually notify the promise. This is needed as the
        // user may already fire events through the pipeline in the ChannelFutureListener.
        // 触发Handler添加事件
        pipeline.invokeHandlerAddedIfNeeded();

        safeSetSuccess(promise);
        // 触发事件Channel注册事件
        pipeline.fireChannelRegistered();
        .......
    } 
}

//NioServerSocketChannel.AbstractNioChannel.doRegister
@Override
protected void doRegister() throws Exception {
    boolean selected = false;
    for (;;) {
        try {
            // 这个方法不在内部类NioMessageUnsafe中，而是在 NioServerSocketChannel.AbstractChannel 类里面
            // 这时候里面this对象就是 NioServerSocketChannel
            // javaChannel() 得到java NIO 的Channel 
            // java NIO 底层的注册 NioServerSocketChannel 作为attachment附件对象
            // 0 标示不关心如何事件 bind 端口的时候换事件
            // 第一次Register并非监听OP_READ，而是0
            // 通过"Register"完成后的fireChannelActive 
            // 触发ChannelActive事件开始传播，最终会调用AbstractNioUnsafe#beginRead()方法：
            // 通过doBeginRead()修改selectionKey 将0改为accept事件
            selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
            return;
        } catch (CancelledKeyException e) {
            if (!selected) {
                // Force the Selector to select now as the "canceled" SelectionKey may still be
                // cached and not removed because no Select.select(..) operation was called yet.
                eventLoop().selectNow();
                selected = true;
            } else {
                // We forced a select operation on the selector before but the SelectionKey is still cached
                // for whatever reason. JDK bug ?
                throw e;
            }
        }
    }
}

// AbstractNioUnsafe#beginRead() 修改selectionKey 将0改为accept事件
@Override
protected void doBeginRead() throws Exception {
    // Channel.read() or ChannelHandlerContext.read() was called
    final SelectionKey selectionKey = this.selectionKey;
    if (!selectionKey.isValid()) {
        return;
    }
    readPending = true;
    final int interestOps = selectionKey.interestOps();
    if ((interestOps & readInterestOp) == 0) {
        // 修改selectionKey 为accept时间
        selectionKey.interestOps(interestOps | readInterestOp);
    }
}

```





### 端口绑定 

AbstractChannel.AbstractUnsafe.bind() 入口

​		doBind() 

​				javaChannel().bind(localAddress, config.getBacklog());  java底层的绑定

​        pipeline.fireChannelActive()  调用ChannelActive传播事件

​				DefaultChannelPipeline.HeadContext.channelActive.readIfIsAutoRead()						

​						AbstractUnsafe.unsafe.beginRead();

​								AbstractNioChannel.doBeginRead()	修改channel关心事件为accept									



```java
//AbstractChannel.AbstractUnsafe.bind()
@Override
public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
    .....	
	// 因为没有绑定端口所以是发了se
    boolean wasActive = isActive();
    try {
        // 调用java底层绑定完端口
        // 完成后 isActive()为true
        doBind(localAddress);
    } catch (Throwable t) {
        safeSetFailure(promise, t);
        closeIfClosed();
        return;
    }
	// 绑定完端口完成后 isActive()为true	
    if (!wasActive && isActive()) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                // 传播ChannelActive事件
                // readIfIsAutoRead调用AbstractNioChannel.doBeginRead()	修改channel关心事件为accept	
                pipeline.fireChannelActive();
            }
        });
    }
}

// 调用java底层绑定完端口
protected void doBind(SocketAddress localAddress) throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
        // 调用java底层绑定完端口
        javaChannel().bind(localAddress, config.getBacklog());
    } else {
        javaChannel().socket().bind(localAddress, config.getBacklog());
    }
}


// DefaultChannelPipeline.channelActive
public void channelActive(ChannelHandlerContext ctx) {
    // 调用传播事件
    ctx.fireChannelActive();
	// 调用AbstractNioChannel.doBeginRead()	修改channel关心事件为accept
    readIfIsAutoRead();
}

//AbstractNioChannel.doBeginRead
protected void doBeginRead() throws Exception {
    // Channel.read() or ChannelHandlerContext.read() was called
    final SelectionKey selectionKey = this.selectionKey;
    if (!selectionKey.isValid()) {
        return;
    }

    readPending = true;

    final int interestOps = selectionKey.interestOps();
    if ((interestOps & readInterestOp) == 0) {
        //  | 为添加channel关心事件为accept
        //  readInterestOp NioServerSocketChannel初始化的时候赋值为accept
        selectionKey.interestOps(interestOps | readInterestOp);
    }
}
```









































​	