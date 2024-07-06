### I/O 模型

#### Java 共支持 3 种网络编程 I/O 模型：BIO、NIO、AIO。

- Java BIO：同步阻塞（传统阻塞型），服务器实现模式为一个连接一个线程，即客户端有连接请求时服务器端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的线程开销。

- Java NIO：同步非阻塞，服务器实现模式为一个线程处理多个请求（连接），即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有 I/O 请求就进行处理。

- Java AIO(NIO.2)：异步非阻塞，AIO 引入异步通道的概念，采用了 Proactor 模式，简化了程序编写，有效的请求才启动线程，它的特点是先由操作系统完成后才通知服务端程序启动线程去处理，一般适用于连接数较多且连接时间较长的应用。

需要注意的是，Java 的 NIO 并不等同于操作系统层面上的 NIO，Java NIO 实际上是基于 IO 多路复用模型的，同时所用的 NIO 组件在 Linux 系统上是使用 epoll 

系统调用实现的。这一点我一开始也弄混了，看了书才搞清楚。

#### BIO、NIO、AIO 使用场景分析

1. BIO 方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，JDK1.4 以前的唯一选择，但程序简单易理解。
2. NIO 方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，弹幕系统，服务器间通讯等。编程比较复杂，JDK1.4 开始支持。
3. AIO 方式使用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用 OS 参与并发操作，编程比较复杂，JDK7 开始支持。
   



### Selector 多路复用器

Selector（多路复用器）是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如读写事件做好准备的组件。这样，一个单独的线程可以管理多个channel，从而管理多个网络连接



#### Selector的创建

通过调用Selector.open()方法创建一个Selector，

```java
Selector selector = Selector.open();
```

1. isOpen() —— 判断Selector是否处于打开状态。Selector对象创建后就处于打开状态了
2. close() —— 当调用了Selector对象的close()方法，就进入关闭状态.。**用完Selector后调用其close()方法会关闭该Selector，且使注册到该Selector上的所有SelectionKey实例无效。通道本身并不会关闭**



#### 向Selector注册通道

为了将Channel和Selector配合使用，必须将channel注册到selector上。

通过SelectableChannel。register()方法来实现。

```java
channel.configureBlocking(false);
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
```

**与Selector一起使用时，Channel必须处于非阻塞模式下**

channel.configureBlocking(false); 这句必须的

register()方法的第二个参数SelectionKey.OP_ACCEPT，这是一个**interest集合**，意思是在通过Selector监听Channel时对什么事件感兴趣

可以监听四种不同类型的事件：

- Connect
- Accept
- Read
- Write

监听的Channel通道触发的一个事件，表示该事件已经就绪

1. 一个channel成功连接到另一个服务器称为”连接就绪“。 （一般客户端去去注册）
2. 一个server socket channel准备号接收新进入的连接称为”接收就绪“。 （一般服务端去注册）
3. 一个有数据可读的通道可以说是”读就绪“。 
4. 一个等待写数据的通道可以说是”写就绪“。

这四种事件用SelectionKey的四个常量来表示：

1. SelectionKey.OP_CONNECT
2. SelectionKey.OP_ACCEPT
3. SelectionKey.OP_READ
4. SelectionKey.OP_WRITE





### SelectionKey详解

SelectionKey：表示Channel 在 Selector 中的注册的标记/句柄

出现的地方：

- register()返回值SelectionKey
- Selector中的SelectionKey集合



只要ServerSocketChannel及SocketChannel向Selector注册了特定的事件，Selector就会监控这些事件是否发生

一个Selector对象会包含3种类型的SelectionKey集合：

- all-keys集合 —— 当前所有向Selector注册的SelectionKey的集合，Selector的keys()方法返回该集合
- selected-keys集合 —— 相关事件已经被Selector捕获的SelectionKey的集合，Selector的selectedKeys()方法返回该集合
- cancelled-keys集合 —— 已经被取消的SelectionKey的集合，Selector没有提供访问这种集合的方法



register()方法执行时，新建一个SelectioKey，并把它加入Selector的all-keys集合中

调用Selector的select()方法时，如果与SelectionKey相关的事件发生了，这个SelectionKey就被加入到selected-keys集合中，程序直接调用selected-keys集合的remove()犯法，或者调用它的iterator的remove()方法，都可以从selected-keys集合中删除一个SelectionKey对象



SelectionKey对象属性：

1. Interest Set兴趣集合
2. Ready Set就绪集合
3. Channel通道
4. Selector选择器
5. Attach附加对象



#### 属性 Interest Set

感兴趣的事件集合，可以通过SelectionKey读写interest集合

```java
int interestSet = selectionKey.interestOps();
```

只要ServerSocketChannel及SocketChannel向Selector注册了特定的事件，Selector就会监控这些事件是否发生



#### 属性 Ready Set

Ready Set是通道已经准备就绪的操作的集合，在selector.selectedKeys()后，你得到这个ready set

```java
int readySet = selectionKey.readyOps();  
```

对于Channel中已就绪的事件，我们可以通过下面这种方式去轮询哪些事件已经就绪，并执行相应的业务操作：

```java
//轮询多路复用器接收到的操作
Set<SelectionKey> selectionKeys = selector.selectedKeys();
Iterator<SelectionKey> selectionKeyIte=selectionKeys.iterator();
while(selectionKeyIte.hasNext()){
    selectionKey = selectionKeyIte.next();
    if (key.isAcceptable()){      
 
    }else if (key.isConnectable()){     
 
    }else if (key.isReadable()){        
 
    }else if (key.isWritable()){        
 
    } 
    // 删除已选的key,以防重复处理  
    selectionKeyIte.remove(); 
}

//检测channel中什么事件或操作已经就绪
selectionKey.isAcceptable();
selectionKey.isConnectable();
selectionKey.isReadable();
selectionKey.isWritable();    
```



#### 属性 Channel通道

SelectionKey定位Channel

```java
Channel channel = selectionKey.channel();
```



#### 属性 Selector选择器

SelectionKey定位Selector

```java
Selector selector = selectionKey.selector();
```





#### 属性 Attach附加对象

可以将一个对象或者更多的信息附着到SelectionKey上，这样就能方便的识别某个给定的通道。例如，可以附加与通道一起使用的Buffer，或是包含聚集数据的某个对象

```java
// 注册的时候添加附加对象
newSocketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));

// 就像事件处理的时候获取附加对象
selectionKey.attach(theObject);
Object attachedObj = selectionKey.attachment();
```



### Selector选择就绪的通道

select()方法调用：返回你所感兴趣的事件（连接，接受，读或写）已经准备就绪的那些通道

换句话说，如果你对”读就绪“的通道感兴趣，select()方法会返回读事件已经就绪的那些通道。

```
// 阻塞等待客户端事件发送，这里有超时时间设置
int select = selector.select();
```

- select() —— 阻塞到至少有一个通道在你注册的事件上就绪了
- select(long timeout) —— 和select()一样，除了最长会阻塞timeout毫秒
- selectNow() —— 不会阻塞，不管什么通道就绪都立刻返回；此方法执行非阻塞的选择操作，如果自从上一次选择操作后，没有通道变成可选择的，则此方法直接返回0
- select()方法返回的Int值表示多少通道就绪



一旦调用了select()方法，并且返回值表明有一个或更多个通道就绪了，然后可以通过调用selector的selectorKeys()方法，访问”已选择键集“中的就绪通道

```java
Set selectedKeys = selector.selectedKeys();
```


**可以遍历这个已选择的集合来访问就绪的通道**：

```java
Set selectedKeys = selector.selectedKeys();
Iterator keyIterator = selectedKeys.iterator();
while(keyIterator.hasNext()){
    SelectionKey key = keyIterator.next();
    if (key.isAcceptable()){      // a connection was accepted by a ServerSocketChannel
 
    }else 
    if (key.isConnectable()){     // a connection was eatablished with a remote server
 
    }else
    if (key.isReadable()){        // a channel is ready for reading
 
    }else
    if (key.isWritable()){        // a channel is ready for writing
 
    }
 
    keyIterator.remove();
}
```



**注意每次迭代末尾的remove()调用，Selector不会自己从已选择集中移除SelectioKey实例，必须在处理完通道时自己移除**



### 简单理解

```java
channel.configureBlocking(false);
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

channel.register()方法执行时，新建一个SelectioKey，并把它加入Selector的keys集合中

如果channel与SelectionKey的interestOps事件发生了，这个SelectionKey就被加入到Selector的selectedKeys集合中

SelectionKey的interestOps变量是可以随时改变的

Selector不会去selected-keys集合的remove()

我们处理完成后需要人工remove,就是告诉Selector已经处理完毕

SelectionKey的readyOps变量:表示通道实际发生的操作事件
可能是
SelectionKey.OP_CONNECT 1
SelectionKey.OP_ACCEPT 4
SelectionKey.OP_READ 8
SelectionKey.OP_WRITE 16

可以通过自带的方法得知是什么操作
selectionKey.isAcceptable();
selectionKey.isConnectable();
selectionKey.isReadable();
selectionKey.isWritable(); 


Selector的重要属性：
keys：所有注册了的 SelectionKey集合
selectedKeys：触发了interestOps事件操作的SelectionKey集合    
publicKeys：是 keys 对外暴露的集合，他是不可修改的，防止外人修改，已注册的SelectionKey
publicSelectedKeys： 是 selectedKeys 对外暴露的集合，他是可删除，不可添加
public abstract class SelectorImpl
    extends AbstractSelector {
    
    // The set of keys registered with this Selector
    private final Set<SelectionKey> keys;

    // The set of keys with data ready for an operation
    private final Set<SelectionKey> selectedKeys;

    // Public views of the key sets
    private final Set<SelectionKey> publicKeys;             // Immutable
    private final Set<SelectionKey> publicSelectedKeys;     // Removal allowed, but not addition

    // pending cancelled keys for deregistration
    private final Deque<SelectionKeyImpl> cancelledKeys = new ArrayDeque<>();

    // used to check for reentrancy
    private boolean inSelect;

    protected SelectorImpl(SelectorProvider sp) {
        super(sp);
        keys = ConcurrentHashMap.newKeySet();
        selectedKeys = new HashSet<>();
        publicKeys = Collections.unmodifiableSet(keys);
        publicSelectedKeys = Util.ungrowableSet(selectedKeys);
    }
	.......
}


```


