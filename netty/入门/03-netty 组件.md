### 组件 

#### NioEventLoop 

​	监听端口

​	对应Thead  处理 新链接 和 数据流

#### Channel 

​	连接 

​	Java底层连接的简单封装

#### Bytebuf 

​	数据流

#### Pipeline   

​	逻辑链路  logic chain

#### ChannelHandler

​	数据包的处理 --logic 



### 简单介绍

#### NioEvenLoop 

主要对应Socket例子中的两个循环处理 

- for (;;) 循环处理 新链接

  简单理解--serverSocket.accept()

- for (;;) 循环处理 一个连接多次发送的数据流

  简单理解--HandlerClient.handler()


NioEvenLoop类

```java
 
    @Override
    protected void run() {
        ....
		// 这里循环就是处理链接和数据流的对应  Sstrategy = electStrategy.SELECT
        for (;;) {
			.....
            //这里就是处理连接的Channel    
            processSelectedKeys();
        }
	}
	
	// 这里就是处理连接的Channel
	private void processSelectedKeys() {
        if (selectedKeys != null) {
            // 
            processSelectedKeysOptimized();
        } 
        ......
    }
	// 这里就是处理连接的Channel
	private void processSelectedKeysOptimized() {
		......
        // 这里就是处理连接的Channel  
        if (a instanceof AbstractNioChannel) {
            processSelectedKey(k, (AbstractNioChannel) a);
        }
    }
	// 最终处理NIO Channel的地方	
    private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
		.....
        if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
            	// 这里对应 READ 和 ACCEPT 的Channel的处理
            	// unsafe接口 对应两个实现类
            	// NioByteUnsafe 处理Channel数据流的读写
                // NioMessageUnsafe 对应新连接的处理
                unsafe.read();
        }
        .....
    }
   
		
```





#### Channel 

对Java底层连接的简单封装

在封装里进行数据流的读写

NO 对应Socket 

NIO 对应SockectChannel

简单理解--socket



源码：

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.78.Final</version>
</dependency>
```



NioEvenLoop  -> run() ->  processSelectedKeys() -> processSelectedKeysOptimized() 

-> processSelectedKey(SelectionKey k, AbstractNioChannel ch)  

-> AbstractNioChannel.NioUnsafe.read()   



AbstractNioByteChannel.read()   对应 数据流处理 

AbstractNioMessageChannel.read()   对应l连接的处理 



NioServerSocketChannel.doReadMessages（）

SocketChannel 对应Java底层NIO

```java

@Override
protected int doReadMessages(List<Object> buf) throws Exception {
    //对应java底层的NIO
    SocketChannel ch = SocketUtils.accept(javaChannel());

    try {
        if (ch != null) {
            buf.add(new NioSocketChannel(this, ch));
            return 1;
        }
    } catch (Throwable t) {
        logger.warn("Failed to create a new channel from an accepted socket.", t);

        try {
            ch.close();
        } catch (Throwable t2) {
            logger.warn("Failed to close a socket.", t2);
        }
    }

    return 0;
}
```



通过上的代码分析netty的 Channel 其实就是对应 Java底层NIO Channel的处理



Channle根据协议不同和阻塞方式不同有很多不同实现：

1. NioSocketChannel：代表异步的客户端 TCP Socket 连接
2. NioServerSocketChannel：异步的服务器端 TCP Socket 连接
3. NioDatagramChannel：异步的 UDP 连接
4. NioSctpChannel：异步的客户端 Sctp 连接
5.  NioSctpServerChannel：异步的 Sctp 服务器端连接
6.  OioSocketChannel：同步的客户端 TCP Socket 连接
7. OioServerSocketChannel：同步的服务器端 TCP Socket 连接
8. OioDatagramChannel：同步的 UDP 连接
9. OioSctpChannel：同步的 Sctp 服务器端连接
10. OioSctpServerChannel：同步的客户端 TCP Socket 连接
    





#### Bytebuf 

IO的读写



#### Pipeline   

逻辑链路  logic chain

对应Socket demo 处理数据的部分

```java
 				// 等待客户端多次输入
                while ((len = inputStream.read(date)) != -1) {
                    String message = new String(date, 0, len);
                    System.out.println("客户端传来消息：" + message);
                    // 返回客户端数据  "\n" 标示符，客户端自己处理
                    String result = "收到数据：" + message + "\n";
                    // 给客户端返回数据
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(result.getBytes(StandardCharsets.UTF_8));
                }
```



NioServerSocketChannel.doReadMessages（）对应Java底层NIO 里面 

初始化Pipeline  

```java
@Override
protected int doReadMessages(List<Object> buf) throws Exception {
    //对应java底层的NIO
    SocketChannel ch = SocketUtils.accept(javaChannel());

    try {
        if (ch != null) {
            // 这里new NioSocketChannel()顺便初始化Pipeline   
            buf.add(new NioSocketChannel(this, ch));
            return 1;
        }
    } catch (Throwable t) {
        logger.warn("Failed to create a new channel from an accepted socket.", t);

        try {
            ch.close();
        } catch (Throwable t2) {
            logger.warn("Failed to close a socket.", t2);
        }
    }

    return 0;
}

// 这里NioSocketChannel的父类构造器里newChannelPipeline()
protected AbstractChannel(Channel parent) {
        this.parent = parent;
        id = newId();
        unsafe = newUnsafe();
        pipeline = newChannelPipeline();
}
// DefaultChannelPipeline.newChannelPipeline()
protected DefaultChannelPipeline(Channel channel) {
    this.channel = ObjectUtil.checkNotNull(channel, "channel");
    succeededFuture = new SucceededChannelFuture(channel, null);
    voidPromise =  new VoidChannelPromise(channel, true);

    tail = new TailContext(this);
    head = new HeadContext(this);

    head.next = tail;
    tail.prev = head;
}

```







#### ChannelHandler

 业务处理 logic 

DefaultChannelPipeline 里面有很多 add 和 remove

需要的参数 ChannelHandler 

这里就是

Pipeline   逻辑链路动态添加 logic 









