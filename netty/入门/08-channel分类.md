### netty 中channel分类 

NioServerSocketChannel

通过反射创建

NioSocketChannel

通过 new NioSocketChannel 去创建

问题思考：为什么NioServerSocketChannel通过反射创建，NioSocketChannel通过 new 去创建？





![](img\2022-09-02 204037.png)



顶层

#### **Channel**  （这是netty自己的接口）

一个网络socket连接或者一系列IO操作的组合 



#### **AbstractChannel** 对 Channel 骨架的抽象

里面有主要的组件 id , unsafe, pipeline, eventLoop;

id , unsafe, pipeline 这三个组件就是在这里创建的

eventLoop 是new NioEventLoopGroup -> new ThreadPerTaskExecutor() ->threadFactory.newThread(command).start() -> 首次execute的时候this.eventLoop = eventLoop 

```java
private final Channel parent;
private final ChannelId id;
private final Unsafe unsafe;
private final DefaultChannelPipeline pipeline;
```



#### **AbstractNioChannel** 用Selector对Channel 的基本实现

Nio 对Channel 的基本实现 ,

```java
// java的Channel
private final SelectableChannel ch; 
// channel关心的就绪事件
protected final int readInterestOp;
// Selector.SelectionKey
volatile SelectionKey selectionKey;

protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent);
    this.ch = ch;
    this.readInterestOp = readInterestOp;
    try {
        // 设置非阻塞
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



#### **接下来分两个分支：一个客户端Channel，一个服务端Channel **



#### **客户端：AbstractNioByteChannel**

它关心的是SelectionKey.OP_READ事件

对应的unsafe是NioByteUnsafe 

NioByteUnsafe 主要读取IO数据

NioByteUnsafe.read()  主要读取IO数据

```java
protected AbstractNioByteChannel(Channel parent, SelectableChannel ch) {
	super(parent, ch, SelectionKey.OP_READ);
}

@Override
protected AbstractNioUnsafe newUnsafe() {
    return new NioByteUnsafe();
}

```







#### **NioSocketChannel**  客户端channel的具体实现

config ：new NioSocketChannelConfig(this, socket.socket());   客户端channel对应的一些配置信息

openSocketChannel ： 对应newChannel

unsafe：对应的unsafe是NioSocketChannelUnsafe相对于NioByteUnsafe就覆盖了一个方法

```java
private final SocketChannelConfig config;

private static SocketChannel newChannel(SelectorProvider provider, InternetProtocolFamily family) {
    try {
        SocketChannel channel = SelectorProviderUtil.newChannel(OPEN_SOCKET_CHANNEL_WITH_FAMILY, provider, family);
        return channel == null ? provider.openSocketChannel() : channel;
    } catch (IOException e) {
        throw new ChannelException("Failed to open a socket.", e);
    }
}

 public NioSocketChannel(Channel parent, SocketChannel socket) {
     super(parent, socket);
     // config赋值
     config = new NioSocketChannelConfig(this, socket.socket());
 }


protected AbstractNioUnsafe newUnsafe() {
    return new NioSocketChannelUnsafe();
}


```



#### **NioByteUnsafe 主要读取IO数据**

主要是read方法

```java
protected class NioByteUnsafe extends AbstractNioUnsafe {

        @Override
        public final void read() {
            final ChannelConfig config = config();
            if (shouldBreakReadReady(config)) {
                clearReadPending();
                return;
            }
            final ChannelPipeline pipeline = pipeline();
            final ByteBufAllocator allocator = config.getAllocator();
            final RecvByteBufAllocator.Handle allocHandle = recvBufAllocHandle();
            allocHandle.reset(config);

            ByteBuf byteBuf = null;
            boolean close = false;
            try {
                do {
                    byteBuf = allocHandle.allocate(allocator);
                    allocHandle.lastBytesRead(doReadBytes(byteBuf));
                    if (allocHandle.lastBytesRead() <= 0) {
                        // nothing was read. release the buffer.
                        byteBuf.release();
                        byteBuf = null;
                        close = allocHandle.lastBytesRead() < 0;
                        if (close) {
                            // There is nothing left to read as we received an EOF.
                            readPending = false;
                        }
                        break;
                    }

                    allocHandle.incMessagesRead(1);
                    readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                    byteBuf = null;
                } while (allocHandle.continueReading());

                allocHandle.readComplete();
                pipeline.fireChannelReadComplete();

                if (close) {
                    closeOnRead(pipeline);
                }
            } catch (Throwable t) {
                handleReadException(pipeline, byteBuf, t, close, allocHandle);
            } finally {
                // Check if there is a readPending which was not processed yet.
                // This could be for two reasons:
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                //
                // See https://github.com/netty/netty/issues/2254
                if (!readPending && !config.isAutoRead()) {
                    removeReadOp();
                }
            }
        }
    }
```





#### **服务端：** AbstractNioMessageChannel

unsafe: NioMessageUnsafe

NioMessageUnsafe 主要读取IO连接

NioMessageUnsafe.read() 主要读取IO连接

```java
protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent, ch, readInterestOp);
}


@Override
protected AbstractNioUnsafe newUnsafe() {
    return new NioMessageUnsafe(); 
}
```



#### **NioServerSocketChannel** 服务端channel的具体实现

openSocketChannel :  对应newChannel

config : new NioServerSocketChannelConfig    服务端channel对应的一些配置信息

它关心的是SelectionKey.OP_ACCEPT事件

```java
private static ServerSocketChannel newChannel(SelectorProvider provider, InternetProtocolFamily family) {
    try {
        ServerSocketChannel channel =
            SelectorProviderUtil.newChannel(OPEN_SERVER_SOCKET_CHANNEL_WITH_FAMILY, provider, family);
        return channel == null ? provider.openServerSocketChannel() : channel;
    } catch (IOException e) {
        throw new ChannelException("Failed to open a socket.", e);
    }
}

private final ServerSocketChannelConfig config;

public NioServerSocketChannel(ServerSocketChannel channel) {
    super(null, channel, SelectionKey.OP_ACCEPT);
    config = new NioServerSocketChannelConfig(this, javaChannel().socket());
}
```





#### NioMessageUnsafe 主要读取IO连接

主要是read方法

```java
 private final class NioMessageUnsafe extends AbstractNioUnsafe {

        private final List<Object> readBuf = new ArrayList<Object>();

        @Override
        public void read() {
            assert eventLoop().inEventLoop();
            final ChannelConfig config = config();
            final ChannelPipeline pipeline = pipeline();
            final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
            allocHandle.reset(config);

            boolean closed = false;
            Throwable exception = null;
            try {
                try {
                    do {
                        int localRead = doReadMessages(readBuf);
                        if (localRead == 0) {
                            break;
                        }
                        if (localRead < 0) {
                            closed = true;
                            break;
                        }

                        allocHandle.incMessagesRead(localRead);
                    } while (continueReading(allocHandle));
                } catch (Throwable t) {
                    exception = t;
                }

                int size = readBuf.size();
                for (int i = 0; i < size; i ++) {
                    readPending = false;
                    pipeline.fireChannelRead(readBuf.get(i));
                }
                readBuf.clear();
                allocHandle.readComplete();
                pipeline.fireChannelReadComplete();

                if (exception != null) {
                    closed = closeOnReadError(exception);

                    pipeline.fireExceptionCaught(exception);
                }

                if (closed) {
                    inputShutdown = true;
                    if (isOpen()) {
                        close(voidPromise());
                    }
                }
            } finally {
                // Check if there is a readPending which was not processed yet.
                // This could be for two reasons:
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                //
                // See https://github.com/netty/netty/issues/2254
                if (!readPending && !config.isAutoRead()) {
                    removeReadOp();
                }
            }
        }
    }
 }
```










