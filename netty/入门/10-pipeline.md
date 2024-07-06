### 常见问题

netty 如何判断ChannelHandler的类型？

答：

有mask标记位来标记，    inbound事件:和MASK_ONLY_INBOUND 比较得到 ， outbound事件:和MASK_ONLY_OUTBOUND比较得到



对于ChannelHandler的添加应该遵循什么样的顺序？

答：

Inbound事件的顺序是正向

Outbound事件的顺序是逆向

异常传播顺序是正向



用户手动触发事件传播，不同的触发方式有什么的区别？

答：

Inbound事件的通过pipeline就从Head传播，ctx则从当前节点向下传播

Outbound事件的通过pipeline就从Tail传播，ctx则从当前节点向上传播

异常传播通过pipeline就从Head传播，ctx则从当前节点向下传播



### pipeline 

1. pipeline初始化
2. 添加删除ChannelHandler
3. 事件和异常的传播



### pipeline初始化

pipeline在channel创建的时候被创建 DefaultChannelPipeline

pipeline节点数据结构ChannelHandlerContext

pipeline的两大哨兵 Head 和 Tail



### pipeline流转

服务端：

NioEventLoop.run 里调用processSelectedKey -->NioMessageUnsafe.read()

NioMessageUnsafe.read()里处理 accpet 得到的NioSocketChannel（默认最多循环16次得到16个NioSocketChannel）

给每条NioSocketChannel 传播 channelRead 事件

从Head节点的channelRead开始，然后到Head的下一个节点ServerBootstrapAcceptor里调用 channelRead 事件

ServerBootstrapAcceptor的channelRead主要三件事

1. 添加用户自定义的childHandler
2. 设置option和attr
3. 选择NioEventLoop注册selector
4. 向selector注册读事件

ServerBootstrapAcceptor的channelRead事件没有往下传播，就结束了



16个NioSocketChannel 都调用完成 channelRead 事件后（调用ServerBootstrapAcceptor.channelRead完成后）

传播 channelReadComplete 事件 

从Head开始一直到Tail结束

服务端一般都是不关心channelReadComplete 事件

服务端一般只关心 channelActive ，channelRegistered，handlerAdded



ServerBootstrapAcceptor节点服务端才有，客户端是没有的





客户端：

NioEventLoop.run 里调用processSelectedKey -->NioByteUnsafe.read()

没对应的NioSocketChannel 分配好Buff，可能一次没有读完，需要读多次，最多16次

每读一次，就传播 channelRead 事件

从Head节点的channelRead开始，

然后到Head的下一个节点就到用户自定义的节点HttpServerCodec

接着到下一个节点HttpObjectAggregator

接着到下一个节点用户重写的SimpleChannelInboundHandler

这个节点调用 writeAndFlush 事件

先从SimpleChannelInboundHandler节点调用write 事件

write 事件从SimpleChannelInboundHandler节点网上传到HttpServerCodec，一直到Head节点（HttpObjectAggregator没有重写write）

在Head节点调用unsafe.write()，写人缓存链表

回到SimpleChannelInboundHandler节点调用flush事件

flush  事件从SimpleChannelInboundHandler节点网上传到HttpServerCodec，一直到Head节点

在Head节点调用unsafe.flush()，将缓存链表写到客户端



读完数据之后调用

传播 channelReadComplete 事件









### pipeline在channel创建的时候被创建



新的AbstractNioChannel创建时，会创建该Channel对应的DefaultChannelPipeline

是一个双向链表结构，链表中节点元素为ChannelHandlerContext



DefaultChannelPipeline创建时，会自动创建并向链表中添加两个ChannelhandlerContext节点——head和tail



tail节点 和 head节点在这个内部类中



pipeline的fireXXX()方法：传播事件的发起方法。会产生相应回调事件并将其交给pipeline中的下一个处理节点

此方法提供给用户实现的ChannelHandler使用，用于将回调事件向pipeline中的下一个节点传递



```java
//AbstractChannel构造器的时候调用newChannelPipeline去创建
protected AbstractChannel(Channel parent) {
    this.parent = parent;
    id = newId();
    unsafe = newUnsafe();
    pipeline = newChannelPipeline();
}

// new DefaultChannelPipeline
protected DefaultChannelPipeline newChannelPipeline() {
    return new DefaultChannelPipeline(this);
}
// pipeline节点数据结构有默认的 head 和 tail 
protected DefaultChannelPipeline(Channel channel) {
    // 保存当前channel
    this.channel = ObjectUtil.checkNotNull(channel, "channel");
    
    succeededFuture = new SucceededChannelFuture(channel, null);
    voidPromise =  new VoidChannelPromise(channel, true);
	// tail节点 --this 等于	当前pipeline节点
    tail = new TailContext(this);
    // head节点 --this 等于	当前pipeline节点
    head = new HeadContext(this);

    head.next = tail;
    tail.prev = head;
}

```







### pipeline节点数据结构ChannelHandlerContext

事件处理器上下文，pipeline中的实际处理节点，数据结构为链表



每个处理节点ChannelHandlerContext中包含一个具体的事件处理器ChannelHandler



ChannelHandlerContext继承了ChannelInboundInvoker和ChannelOutboundInvoker



ChannelHandlerContext的主要实现类是AbstractChannelHandlerContext，AbstractChannelHandlerContext实现了大部分功能



fireChannelRead和write都用实现了

fireChannelRead：从Head的channelRead开始

write：从Tail的write开始



对channelActive和channelReadComplete事件，向后传递并产生了Channel.read()的outbound事件

最终调用unsafe.beginRead()：channel向注册selector注册感兴趣的事件SelectionKey.OP_READ | SelectionKey.OP_ACCEPT





Inbound事件 ：**用于描述因外部事件导致的Channel状态变更**

outbound事件：**用于定义Channel能够提供的IO操作**



```java
//ChannelHandlerContext的主要实现类是AbstractChannelHandlerContext
public interface ChannelHandlerContext extends AttributeMap, ChannelInboundInvoker, ChannelOutboundInvoker {
    // 需要了解 pipeline节点对应的channel
    Channel channel();
	// 哪一个NioEventLoop去执行
    EventExecutor executor();
	// ChannelHandlerContext都有一个名字
    String name();
	// 业务处理器-真正做事情的
    ChannelHandler handler();
	// 是否被删除
    boolean isRemoved();
	
    //----inbound的事件----start--
    //最主要的是registered active channelRead 
    //inbound主要ChannelRead,将buff进行转换
    //fireXXX()，用于发起一个inbound事件
    ChannelHandlerContext fireChannelRegistered();

    ChannelHandlerContext fireChannelUnregistered();

    ChannelHandlerContext fireChannelActive();

    ChannelHandlerContext fireChannelInactive();

    ChannelHandlerContext fireExceptionCaught(Throwable cause);

    ChannelHandlerContext fireUserEventTriggered(Object evt);

    ChannelHandlerContext fireChannelRead(Object msg);

    ChannelHandlerContext fireChannelReadComplete();
	//chanele默认write缓存链表，超过64k,就不写，低于32k,才可以再次写入
    //可写状态更改
    ChannelHandlerContext fireChannelWritabilityChanged();
	//----inbound的事件----end--
    
    //----Outbound的事件----start--
    //最终调用unsafe.beginRead()：channel向注册selector注册感兴趣的事件SelectionKey.OP_READ | SelectionKey.OP_ACCEPT
    ChannelHandlerContext read();

    ChannelHandlerContext flush();
     //----Outbound的事件----end--

	//当前pipeline
    ChannelPipeline pipeline();

 	// 内存分配器
    ByteBufAllocator alloc();


}

//主要是registered，Active,channelRead事件
//最主要的是registered active channelRead 
//inbound主要ChannelRead,将buff进行转换
//fireXXX()，用于发起一个inbound事件
//对channelActive和channelReadComplete事件，向后传递并产生了Channel.read()的outbound事件
public interface ChannelInboundInvoker {
	// 注册完成之后调用的
    ChannelInboundInvoker fireChannelRegistered();
	//  Channel被从NioEventLoop中取消注册完成之后调用的
    ChannelInboundInvoker fireChannelUnregistered();
	// Channel的状态可用 Active
    ChannelInboundInvoker fireChannel Active();
	// Channel的状态不可用 Active
    ChannelInboundInvoker fireChannelInactive();
	// 异常处理-顺序传播
    ChannelInboundInvoker fireExceptionCaught(Throwable cause);
	// 用户产生的自定义事件
    ChannelInboundInvoker fireUserEventTriggered(Object event);
	// 将buff或者数据进行转换--IO数据的读取
    ChannelInboundInvoker fireChannelRead(Object msg);
	// 将buff或者数据转换完成，不必继续向下传播ChannelRead事件
    ChannelInboundInvoker fireChannelReadComplete();
    //chanele默认write缓存链表，超过64k,就不写，低于32k,才可以再次写入
    //chanele可写状态更改
    ChannelInboundInvoker fireChannelWritabilityChanged();
}

// 用户主动发起的请求，主要是write事件,write事件将数据放到缓存区，等待flush,写到客户端
// IO的读写基本都是交由unsafe处理的
public interface ChannelOutboundInvoker {
	//  Channel发起bind操作，启动的时候绑定多个端口
    ChannelFuture bind(SocketAddress localAddress);
	//  Channel发起connect操作，客户端操作比较多
    ChannelFuture connect(SocketAddress remoteAddress);
	//  Channel发起connect操作，客户端操作比较多
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress);
	//  Channel发起断开connect操作
    ChannelFuture disconnect();
	//  Channel发起关闭操作
    ChannelFuture close();
	//  Channel从NioEventLoop中取消注册
    ChannelFuture deregister();
	//  Channel发起bind操作，启动的时候绑定多个端口
    ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise);
	//  Channel发起connect操作
    ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise);
	//  Channel发起connect操作
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);
	//  Channel发起断开connect操作，同时产生回调
    ChannelFuture disconnect(ChannelPromise promise);
	//  Channel发起关闭操作，同时产生回调
    ChannelFuture close(ChannelPromise promise);
	//  Channel从NioEventLoop中取消注册，同时产生回调
    ChannelFuture deregister(ChannelPromise promise);
	//请求将数据从通道读取到第一个入站缓冲区，
    //如果数据被读取，触发ChannelInboundHandler.channelRead（ChannelHandlerContext，Object）事件，
    //并触发channelReadComplete事件，以便处理程序可以决定继续读取。
    //如果已经存在挂起的读取操作，则此方法不执行任何操作。 
    //最终调用unsafe.beginRead()：channel向注册selector注册感兴趣的事件SelectionKey.OP_READ | SelectionKey.OP_ACCEPT
    ChannelOutboundInvoker read();
	//write事件将数据放到缓存区，等待flush,写到客户端
    ChannelFuture write(Object msg);

    ChannelFuture write(Object msg, ChannelPromise promise);
	//flush将数据缓存区写到客户端
    ChannelOutboundInvoker flush();
	////write事件将数据放到缓存区后立即调用flush将数据缓存区写到客户端
    ChannelFuture writeAndFlush(Object msg, ChannelPromise promise);

    ChannelFuture writeAndFlush(Object msg);

    ChannelPromise newPromise();

    ChannelProgressivePromise newProgressivePromise();

    ChannelFuture newSucceededFuture();

    ChannelFuture newFailedFuture(Throwable cause);

    ChannelPromise voidPromise();
}

// 这个类实现了ChannelHandlerContext的大部分功能
//fireChannelRead和write都用实现，
//fireChannelRead从Head开始
//write从Tail开始
abstract class AbstractChannelHandlerContext implements ChannelHandlerContext, ResourceLeakHint {
	
    //这两属性是主要依靠
    volatile AbstractChannelHandlerContext next;
    volatile AbstractChannelHandlerContext prev;
    
    private final DefaultChannelPipeline pipeline;
    private final String name;

    AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutor executor,
                                  String name, Class<? extends ChannelHandler> handlerClass) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.pipeline = pipeline;
        this.executor = executor;
        // mask标记是否inbound事件和outbound事件
        // inbound事件:MASK_ONLY_INBOUND  
        // outbound事件:MASK_ONLY_OUTBOUND
        // allInbound事件:MASK_ALL_INBOUND
        // allOutbound事件:MASK_ALL_OUTBOUND
        // 通过mask和MASK_ONLY_INBOUND比较就可以得知是否是inbound事件
        // 通过mask和MASK_ONLY_OUTBOUND比较就可以得知是否是outbound事件
        this.executionMask = mask(handlerClass);
        // Its ordered if its driven by the EventLoop or the given Executor is an instanceof OrderedEventExecutor.
        ordered = executor == null || executor instanceof OrderedEventExecutor;
    }
    
    ......
	
}


```



Netty 会判断 Handler 的类型是否是 ChannelInboundHandler 的实例，如果是会把所有 Inbound 事件先置为 1，然后排除 Handler 不感兴趣的方法。

同理，Handler 类型如果是 ChannelOutboundHandler，也是这么实现的



对于 Inbound 事件，会先从 HeadContext 节点开始传播，所以 unsafe 可以看作是 Inbound 事件的发起者；

对于 Outbound 事件，数据最后又会经过 HeadContext 节点返回给客户端，此时 unsafe 可以看作是 Outbound 事件的处理者



如何排除 Handler 不感兴趣的事件呢？Handler 对应事件的方法上如果有 @Skip 注解，Netty 认为该事件是需要排除的。

大部分情况下，用户自定义实现的 Handler 只需要关心个别事件，那么剩余不关心的方法都需要加上 @Skip 注解吗？

Netty 其实已经在 ChannelOutboundHandlerAdapter 和ChannelInboundHandlerAdapter 中默认都添加好了。

所以用户如果继承了 ChannelOutboundHandlerAdapter 或者 ChannelInboundHandlerAdapter ，默认没有重写的方法都是加上 @Skip 的，只有用户重写的方法才是 Handler 关心的事件。



具体实现在mask(handlerClass) 方法里



### pipeline的两大哨兵 Head 和 Tail

Tail节点：是处理bytes and messages的最后一个屏障，同时也是作为pipeline的第一环向上传递outbound事件，是一个Inbound处理节点

数据传播的最后处理，Tail节点只是向上传播outbound事件，本身并不是outbound处理节点

例如：

exceptionCaught:

业务处理出现异常，没有捕获，最后到了Tail ， Tail 会捕获并打印一个warn,同时释放buff

channelRead:

读消息到了这里，它也会打印告诉你消息到了这被丢弃了，让你检查配置，并同时释放buff

write：

作为pipeline的第一环向上传递write事件



```java
//write事件会在Head里面用unsafe处理，Tail只是传播的起点
//作为pipeline的第一环向上传递write事件
channelHandlerContext.channel().write(response);
//当前节点向上传播
channelHandlerContext.write(response);
```

```java
// TailContext是没有
final class TailContext extends AbstractChannelHandlerContext implements ChannelInboundHandler {

        TailContext(DefaultChannelPipeline pipeline) {
            super(pipeline, null, TAIL_NAME, TailContext.class);
            setAddComplete();
        }

        @Override
        public ChannelHandler handler() {
            return this;
        }
		//啥都没干
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) { }
		//啥都没干
        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) { }
		//啥都没干
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            onUnhandledInboundChannelActive();
        }
		//啥都没干
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            onUnhandledInboundChannelInactive();
        }
		//啥都没干
        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) {
            onUnhandledChannelWritabilityChanged();
        }
		//啥都没干
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) { }
		//啥都没干
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) { }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            // 用户自定义将计数器释放，兜底重置
            onUnhandledInboundUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // 异常兜底
            onUnhandledInboundException(cause);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 数据传播兜底
            onUnhandledInboundMessage(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            // 数据传播完成兜底
            onUnhandledInboundChannelReadComplete();
        }
    }
```





Head节点：

节点既是inBound处理节点，又是outBound处理节点

主要做两件是

1. 将读写事件向下传播 -- netty每次读写事件都会从Head开始向下传播
2. 读写操作的时候都会委托到unsafe去操作



readIfIsAutoRead(); 会去触发unsafe.beginRead();然后去注册readInterestOp



```java
// A special catch-all handler that handles both bytes and messages.
// Tail 是处理bytes and messages的最后一个屏障，是一个Inbound事件
// 读数据的最后处理
// 例如：
// exceptionCaught:
// 业务处理出现异常，没有捕获，最后到了Tail ， Tail 会捕获并打印一个warn,同时释放buff
// channelRead:
// 读消息到了这里，它也会打印告诉你消息到了这被丢弃了，让你检查配置，并同时释放buff
final class TailContext extends AbstractChannelHandlerContext implements ChannelInboundHandler {

    TailContext(DefaultChannelPipeline pipeline) {
        super(pipeline, null, TAIL_NAME, TailContext.class);
        setAddComplete();
    }

    @Override
    public ChannelHandler handler() {
        return this;
    }
	//啥都没干
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) { }
	//啥都没干
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) { }
	//啥都没干
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        onUnhandledInboundChannelActive();
    }
	//啥都没干
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        onUnhandledInboundChannelInactive();
    }
	//啥都没干
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        onUnhandledChannelWritabilityChanged();
    }
	//啥都没干
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) { }
	//啥都没干
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) { }
	
    // 用户自定义将计数器释放，兜底重置
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        onUnhandledInboundUserEventTriggered(evt);
    }
	// 业务处理出现异常，没有捕获，最后到了Tail ， Tail 会捕获并打印一个warn,同时释放buff
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        onUnhandledInboundException(cause);
    }
	// 读消息到了这里，它也会打印告诉你消息到了这被丢弃了，让你检查配置，并同时释放buff
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        onUnhandledInboundMessage(ctx, msg);
    }
	// 读消息到了这里，它也会打印告诉你消息到了这被丢弃了，让你检查配置，并同时释放buff
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // 数据传播完成兜底
        onUnhandledInboundChannelReadComplete();
    }
}


// 它有一个 unsafe 主要处理底层数据的读写
// 节点既是inBound处理节点，又是outBound处理节点
// 主要做两件是：
// 1.将读写事件向下传播 -- netty每次读写事件都会从Head开始向下传播
// 2.读写操作的时候都会委托到unsafe去操作
final class HeadContext extends AbstractChannelHandlerContext
    implements ChannelOutboundHandler, ChannelInboundHandler {

    private final Unsafe unsafe;

    HeadContext(DefaultChannelPipeline pipeline) {
        super(pipeline, null, HEAD_NAME, HeadContext.class);
        unsafe = pipeline.channel().unsafe();
        setAddComplete();
    }

    @Override
    public ChannelHandler handler() {
        return this;
    }
	//啥都没干
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // NOOP
    }
	//啥都没干
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        // NOOP
    }

    @Override
    public void bind(
        ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        unsafe.bind(localAddress, promise);
    }

    @Override
    public void connect(
        ChannelHandlerContext ctx,
        SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) {
        unsafe.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
        unsafe.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
        unsafe.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) {
        unsafe.deregister(promise);
    }
	
    // 将channel感兴趣的事件注册到seletor中
    @Override
    public void read(ChannelHandlerContext ctx) {
        unsafe.beginRead();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        unsafe.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) {
        unsafe.flush();
    }
	//异常向下传播
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        invokeHandlerAddedIfNeeded();
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        ctx.fireChannelUnregistered();

        // Remove all handlers sequentially if channel is closed and unregistered.
        if (!channel.isOpen()) {
            destroy();
        }
    }
	
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
		// 这里是会去注册对应的SelectionKey事件
        // 会去触发unsafe.beginRead();然后去注册readInterestOp
        readIfIsAutoRead();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
    }
	// 读数据向下传播
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.fireChannelRead(msg);
    }
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();

        readIfIsAutoRead();
    }

    private void readIfIsAutoRead() {
        if (channel.config().isAutoRead()) {
            channel.read();
        }
    }
	// 用户自定义向下传播
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        ctx.fireUserEventTriggered(evt);
    }
	// 用户自定义向下传播
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        ctx.fireChannelWritabilityChanged();
    }
}

```





### ChannelHandler

ChannelHandler（事件处理器接口），由ChannelInboundHandler接口和ChannelOutboundHandler接口继承



ChannelInboundHandler中定义了各个回调事件的回调方法，由用户进行具体实现，主要实现协议（数据的）的处理



ChannelOutboundHandler中定义了方法进行**Channel内部IO操作**（Channel发起bind/connect/close操作，Channel监听OP_READ，Channel写IO数据...），供用户在回调方法中使用



ChannelInboundHandlerAdapter和ChannelOutboundHandlerAdapter为接口的默认实现类（其实没干什么事），

用户通过继承这两个类来实现自己的业务处理逻辑，就可以不需要在实现ChannelHandler接口上加上@Skip注解

默认实现ChannelHandler接口是需要加上@Skip注解的








### pipeline添加ChannelHandler的过程

> 从DefaultChannelPipeline.addLast(ChannelHandler... handlers)这行代码开始跟吧

细节不想写了，列一下大致流程：

1）checkMultiplicity(handler); --> 检查要添加的handler是否可以被重复添加（有@Sharable说明可以重复添加）

2）newCtx= newContext(group,filterName(name,handler),handler); --> 创建节点

3）addLast0(newCtx); --> 添加节点到pipeline的末尾（tail之前）

4）callHandlerAdded0(newCtx); --> 回调新建节点的handlerAdded(ctx)方法

```java
ch.pipeline().addLast(new HttpServerCodec());
```

需要注意的是：

ChannelInitializer 的 handlerAdded

调用 ChannelInitializer 的 handlerAdded 会删除自身

```java
public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isRegistered()) {
        // 调用initChannel，里面会将自身删除
        if (initChannel(ctx)) {
            removeState(ctx);
        }
    }
}

private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
    if (initMap.add(ctx)) { // Guard against re-entrance.
        try {
            //调用自定义的initChannel
            initChannel((C) ctx.channel());
        } catch (Throwable cause) {
            // Explicitly call exceptionCaught(...) as we removed the handler before calling initChannel(...).
            // We do so to prevent multiple calls to initChannel(...).
            exceptionCaught(ctx, cause);
        } finally {
            if (!ctx.isRemoved()) {
                // 删除自身
                ctx.pipeline().remove(this);
            }
        }
        return true;
    }
    return false;
}
```





### pipeline移除ChannelHandler的过程

> 从DefaultChannelPipeline.remove(ChannelHandler handler)这行代码开始跟吧

细节不想写了，列一下大致流程：

1）getContextOrDie(handler) --> 遍历pipeline链表找到待删除的接待对象

2）remove0(ctx) --> 将节点从pipeline链表中移除

3）callHandlerRemoved0(ctx)  --> 回调移除节点的handlerRemoved(ctx)方法。

```java
ctx.pipeline().remove(this);
```









###  ChannelHandler 接口

![](img\2022-09-07 021227.png)



ChannelHandler 基本接口

ChannelHandlerAdapter  对ChannelHandler的基本实现



ChannelInboundHandler 回调事件的回调方法，主要实现协议（数据的）的处理 

ChannelOutboundHandler Channel内部IO操作，一般由用户主动发起的操作



ChannelInboundHandlerAdapter 为继承ChannelHandlerAdapter 及 ChannelInboundHandler 为接口的默认实现类 

ChannelOutboundHandlerAdapter 为继承ChannelHandlerAdapter 及 ChannelOutboundHandler 为接口的默认实现类 



用户通过继承这两个类来实现自己的业务处理逻辑，就可以不需要在实现ChannelHandler接口上加上@Skip注解

```java
// 基本接口
public interface ChannelHandler {
	//ChannelHandler添加的时候回调
    void handlerAdded(ChannelHandlerContext ctx) throws Exception;
	//ChannelHandler删除的时候回调
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception;
	//异常的时候回调，已经移到ChannelInboundHandler里了
    @Deprecated
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
  	//是否可重复添加标示
    @Inherited
    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sharable {
        // no value
    }
}
//相对ChannelHandler添加了一些方法
public interface ChannelInboundHandler extends ChannelHandler {
	//channel注册的时候回调
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;
    //channel取消注册完成之后回调
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;
	//channel可用回调
    void channelActive(ChannelHandlerContext ctx) throws Exception;
	//channel状态失效回调
    void channelInactive(ChannelHandlerContext ctx) throws Exception;
	//channel客户端读到了一些数据，服务端有新连接
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
	//channel都读玩之后的回调
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;
	//用户自定义
    void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception;
	//chanele默认write缓存链表，超过64k,就不写，低于32k,才可以再次写入
    //chanele可写状态更改
    void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception;
	//ChannelHandler异常处理
    @Override
    @SuppressWarnings("deprecation")
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}

//相对ChannelHandler添加了一些IO方面的方法，都是主动触发，
public interface ChannelOutboundHandler extends ChannelHandler {
	//  Channel发起bind操作，启动的时候绑定多个端口
    void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception;
	//  Channel发起connect操作，客户端操作比较多
    void connect(
            ChannelHandlerContext ctx, SocketAddress remoteAddress,
            SocketAddress localAddress, ChannelPromise promise) throws Exception;
	//  Channel发起断开connect操作
    void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;
	//  Channel发起关闭操作
    void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;
    //  Channel从NioEventLoop中取消注册
    void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;
	//请求将数据从通道读取到第一个入站缓冲区
    //开始从Head开始，直接调用unsafe.beginRead()
    //Channel准备好了就是注册完了-SelectionKey.OP_READ | SelectionKey.OP_ACCEPT
    void read(ChannelHandlerContext ctx) throws Exception;
	//write事件将数据放到缓存区，等待flush,写到客户端
    void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;
	//flush将数据缓存区写到客户端
    void flush(ChannelHandlerContext ctx) throws Exception;
}

//ChannelHandlerAdapter对ChannelHandler的基本实现
public abstract class ChannelHandlerAdapter implements ChannelHandler {

    boolean added;

    protected void ensureNotSharable() {
        if (isSharable()) {
            throw new IllegalStateException("ChannelHandler " + getClass().getName() + " is not allowed to be shared");
        }
    }

    public boolean isSharable() {
        Class<?> clazz = getClass();
        Map<Class<?>, Boolean> cache = InternalThreadLocalMap.get().handlerSharableCache();
        Boolean sharable = cache.get(clazz);
        if (sharable == null) {
            sharable = clazz.isAnnotationPresent(Sharable.class);
            cache.put(clazz, sharable);
        }
        return sharable;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    @Skip
    @Override
    @Deprecated
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}

//ChannelInboundHandlerAdapter为继承ChannelHandlerAdapter及ChannelInboundHandler 为接口的默认实现类
//用户通过继承这个类来实现自己的业务处理逻辑，就可以不需要在实现ChannelHandler接口上加上@Skip注解
public class ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler {

    @Skip
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }

    @Skip
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();
    }

    @Skip
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    @Skip
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Skip
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    @Skip
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }

    @Skip
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    @Skip
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelWritabilityChanged();
    }

    @Skip
    @Override
    @SuppressWarnings("deprecation")
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}



//ChannelOutboundHandlerAdapter 为继承ChannelHandlerAdapter 及 ChannelOutboundHandler 为接口的默认实现类 
//用户通过继承这个类来实现自己的业务处理逻辑，就可以不需要在实现ChannelHandler接口上加上@Skip注解
public class ChannelOutboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelOutboundHandler {

    @Skip
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
            ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Skip
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
            SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Skip
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
            throws Exception {
        ctx.disconnect(promise);
    }

    @Skip
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise)
            throws Exception {
        ctx.close(promise);
    }

    @Skip
    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Skip
    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Skip
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    @Skip
    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}











```





### Inbound事件的传播

何为Inbound事件以及ChannelInboundHandler 



channelRead 事件的传播 



SimpleChannelInboundHandler 处理器



#### channelRead 事件的传播 

通过pipeline.fireChannelRead默认传播是从Head节点开始向下传播的 最后到 Tail 节点收尾

```java
ctx.pipeline().fireChannelRead("hello InboundHandler0.qinjp");
```

通过ctx.fireChannelRead则是从当前节点下传播的 最后到 Tail 节点收尾

```java
ctx.fireChannelRead(msg);
```

需要注意：如果下一个节点没有继续ctx.fireChannelRead(msg);

会在当前节点停止，不会到Tail节点

Tail节点兜底-打印啥都不干

```java
protected void onUnhandledInboundMessage(Object msg) {
    try {
        logger.debug(
                "Discarded inbound message {} that reached at the tail of the pipeline. " +
                        "Please check your pipeline configuration.", msg);
    } finally {
        ReferenceCountUtil.release(msg);
    }
}
```



**别的Inbound事件的传播 和 channelRead 传播机制是一样的  正向重播**



![](img\2022-09-07 030540.png)









#### SimpleChannelInboundHandler 处理器

在SimpleChannelInboundHandler 里作用：自动帮你释放内存

我们只有重写channelRead0即可--防止你忘记释放内存

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    boolean release = true;
    try {
        if (acceptInboundMessage(msg)) {
            @SuppressWarnings("unchecked")
            I imsg = (I) msg;
            channelRead0(ctx, imsg);
        } else {
            release = false;
            ctx.fireChannelRead(msg);
        }
    } finally {
        if (autoRelease && release) {
            //帮你释放内存
            ReferenceCountUtil.release(msg);
        }
    }
}


protected abstract void channelRead0(ChannelHandlerContext ctx, I msg) throws Exception;
```







### Outbound事件的传播

何为Outbound事件以及ChannelOutboundHandler



write 事件的传播 



#### write 事件的传播 

通过pipeline.write默认传播是从Tail节点开始上传播的 最后到 Head 节点调用unsafe.write 去向IO写数据

```java
ctx.pipeline().write(msg);
```

通过ctx.write默认传播是从当前节点开始上传播的 最后到 Head 节点调用unsafe.write 去向IO写数据

```java
ctx.write(msg);
```

最后调用Head的write调用unsafe.write(msg, promise);

```java
@Override
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    unsafe.write(msg, promise);
}
```

需要注意：如果下一个节点没有继续ctx.write(msg);

会在当前节点停止，不会到Head节点





**别的Outbound事件的传播 和 write传播机制是一样的  逆向重播**

![](img\2022-09-07 050004.png)







### 异常传播

通过pipeline.fireExceptionCaught默认传播是从Head节点开始向下传播的 最后到 Tail 节点收尾

```java
ctx.pipeline().fireExceptionCaught(cause);
```

通过ctx.fireExceptionCaught则是从当前节点下传播的 最后到 Tail 节点收尾

```java
ctx.fireExceptionCaught(cause);
```

最后调用Tail 的exceptionCaught

Tail节点兜底-打印啥都不干

```java
protected void onUnhandledInboundException(Throwable cause) {
    try {
        logger.warn(
                "An exceptionCaught() event was fired, and it reached at the tail of the pipeline. " +
                        "It usually means the last handler in the pipeline did not handle the exception.",
                cause);
    } finally {
        ReferenceCountUtil.release(cause);
    }
}
```



异常默认传播是从当前节点的异常，一直到Tail节点

中间不区分是Inbound事件或者Outbound事件

因为Inbound事件或者Outbound事件，都有exceptionCaught接口



需要注意：如果下一个节点没有继续ctx.fireExceptionCaught(cause);

会在当前节点停止，不会到Tail节点





![](img\2022-09-07 050911.png)





**异常传播机制是 正向重播**





### 异常的最佳实践

在最后添加一个异常处理器

```java
public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      	System.out.println("==============ExceptionCaughtHandler==============");
        if (cause instanceof BusinessExcetion businessExcetion) {
            businessExcetion.printStackTrace();
        }
        ReferenceCountUtil.release(cause);
        ctx.close();
    }
}


//这个异常处理器放在最后
ch.pipeline().addLast(new ExceptionCaughtHandler());
```





### 总结

pipeline在channel创建的时候被创建 DefaultChannelPipeline

pipeline节点数据结构ChannelHandlerContext是个双向链表

pipeline的默认两大哨兵 Head 和 Tail

Head ：负责具体协议

Tail：兜底操作



添加和删除都是在对应节点进行删除和添加



Inbound事件的传播  正向重播

Outbound事件的传播  逆向重播

异常传播 正向重播，但不区分Inbound事件还是Outbound事件



























