### NIO 三大核心

#### Selector 多路复用器

释放服务器资源

#### Buffer 缓冲区

使用堆外内存，提升内存使用效率

#### Channel 通道

通过channel提供可靠的数据传输



### NIO 的整体流程

用一个线程管理多个客户端连接

![](img\2022-07-20 162541.png)



### 什么是多路复用

NIO相较于BIO,使用多路复用器，可以一个线程管理所有客户端连接

而BIO需要对每个连接创建一个线程

Java 中一个线程管理多个Sockect对象，不叫多路复用

```java
List<Sockect> allSocket = new ArrayList<>;
while(true) {
    Sockect sockect = serverSockect.accpet();
    new Thead(() -> doWork(sockect)).start()
}
void doWork(Sockect sockect) {
    // 新线程处理连接任务
}
```

上层Java应用只是通过多线程的手段使得主线程没有卡在read函数，可系统为我们提供的read还是阻塞的

而一个客户端就开一个线程，对服务器消耗是很大的。所以这种小把戏，并不能真正提高BIO的性能



多路复用是操作系统内核的概念并不是一种实现效果

简单认为：只有使用了select ，pull , epull 三个系统调用才能称为多路复用

 



### 操作系统基础

#### 用户态和内核态

Linux操作系统就将权限等级分为了2个等级，实际上这个态代表的是**当前 CPU 的状态**

**内核态**：系统中既有操做系统的程序，也有普通用户程序。为了安全性和稳定性，操做系统的程序不能随便访问，这就是内核态。

即须要执行操做系统的程序就必须转换到内核态才能执行！！！内核态能够使用计算机全部的硬件资源！！！

**用户态**：不能直接使用系统资源，也不能改变CPU的工做状态，而且只能访问这个用户程序本身的存储空间！！！！



这里引出一个问题：

例如写文件到磁盘，用户态的程序是不能直接操作磁盘的，需要切换到内核态才能真正去操作磁盘，这就涉及到用户态如何切换到内核态



#### 用户态如何切换到内核态

1. 系统调用：操作系统的标准操作，一个最小功能单元，用户程序不能调用这些函数，必须通过操作系统提供的标准函数库去调用
2. 异常：当前进程运行在用户态，如果这个时候发生了异常事件，就会触发切换。例如：缺页异常
3. 外设中断：当外设完成用户的请求时，会向CPU发送中断信号。这时CPU会暂停执行下一条即将要执行的指令而转到与中断信号对应的处理程序去执行，简单来说，程序运行的时候，外设输入，CPU优先处理，外设输入对应的程序，因为前面是用户态下的程序，所以处理外设输入就进入了内核态



#### linux 系统调用

```shell
man syscalls
## 2 标示系统调用
man 2 select
## 7 标示杂项
man 7 epoll
```



#### File Descriptor 文件描述符

文件描述符：简称FD ,形式上是一个非负整数

内核为每一个进程维护的文件记录表

Linux中一切皆文件，指的就是FD

在网络编程中，创建的Socket ,都会以一个FD来描述这个Socket，对Socket的操作都会围绕这个FD展开



#### 跟踪fd

简单java

```java
import java.util.Scanner;

public class BolckDemo {

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final String s = scanner.nextLine();
        System.out.println(s);
    }
}
```

**jps 查询Linux系统当前所有java进程pid的命令**

**lsof命令用于查看你进程开打的文件**

```shell

root@qinjp-Virtual-Machine:/usr/local/java# java BolckDemo.java

root@qinjp-Virtual-Machine:/usr/local/java# jps
1531 Jps
1517 Main

root@qinjp-Virtual-Machine:/usr/local/java# lsof -p 1517
lsof: WARNING: can't stat() fuse.gvfsd-fuse file system /run/user/126/gvfs
      Output information may be incomplete.
COMMAND  PID USER   FD   TYPE             DEVICE  SIZE/OFF    NODE NAME
java    1517 root  cwd    DIR                8,3      4096  526185 /usr/local/java
java    1517 root  rtd    DIR                8,3      4096       2 /
java    1517 root  txt    REG                8,3     12368  526191 /usr/local/java/jdk-17/bin/java
java    1517 root  mem    REG                8,3  13672448  658806 /usr/local/java/jdk-17/lib/server/classes.jsa
java    1517 root  mem    REG                8,3   8876560  395661 /usr/lib/locale/locale-archive
java    1517 root  mem    REG                8,3 126407125  526905 /usr/local/java/jdk-17/lib/modules
java    1517 root  mem    REG                8,3    108432  526894 /usr/local/java/jdk-17/lib/libnet.so
java    1517 root  mem    REG                8,3     99680  526895 /usr/local/java/jdk-17/lib/libnio.so
java    1517 root  mem    REG                8,3    866128  526901 /usr/local/java/jdk-17/lib/libsvml.so
java    1517 root  mem    REG                8,3    190104  526881 /usr/local/java/jdk-17/lib/libjava.so
java    1517 root  mem    REG                8,3    145528  526885 /usr/local/java/jdk-17/lib/libjimage.so
java    1517 root  mem    REG                8,3    940560  395423 /usr/lib/x86_64-linux-gnu/libm.so.6
java    1517 root  mem    REG                8,3     14664  395458 /usr/lib/x86_64-linux-gnu/librt.so.1
java    1517 root  mem    REG                8,3  22784488  658809 /usr/local/java/jdk-17/lib/server/libjvm.so
java    1517 root  mem    REG                8,3   2216304  395418 /usr/lib/x86_64-linux-gnu/libc.so.6
java    1517 root  mem    REG                8,3     14432  395421 /usr/lib/x86_64-linux-gnu/libdl.so.2
java    1517 root  mem    REG                8,3     21448  395454 /usr/lib/x86_64-linux-gnu/libpthread.so.0
java    1517 root  mem    REG                8,3     73056  526886 /usr/local/java/jdk-17/lib/libjli.so
java    1517 root  mem    REG                8,3    108936  402600 /usr/lib/x86_64-linux-gnu/libz.so.1.2.11
java    1517 root  mem    REG                8,3     32768 1324088 /tmp/hsperfdata_root/1517
java    1517 root  mem    REG                8,3    240936  395406 /usr/lib/x86_64-linux-gnu/ld-linux-x86-64.so.2
java    1517 root    0u   CHR              136,0       0t0       3 /dev/pts/0
java    1517 root    1u   CHR              136,0       0t0       3 /dev/pts/0
java    1517 root    2u   CHR              136,0       0t0       3 /dev/pts/0
java    1517 root    3r   REG                8,3 126407125  526905 /usr/local/java/jdk-17/lib/modules
java    1517 root    5u  unix 0xffff8ffe430fe000       0t0   41191 type=STREAM

### 
cwd：表示current work dirctory 当前工作的目录
rtd：root directory 跟目录
txt: 当前运行程序的指令
mem：memory-mapped file 内存映射文件 
mmap：memory-mapped device 内存映射设备 

```



#### strace 查看系统调用指令

```shell
strace -ff -o log java BioServer.java
```



```java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BioServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务启动完成");
        while (true) {
            // 这里会阻塞-等待新的客户端进来
            Socket socket = serverSocket.accept();
            System.out.println("新的连接------" + socket.getRemoteSocketAddress());

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            String result = "收到数据：";
            byte[] date = new byte[1024];
            int len;
            // inputStream.read 会阻塞等待再次输入
            while ((len = inputStream.read(date)) != -1) {
                String message = new String(date, 0, len);
                result = result + message;
            }
            // 返回客户端数据  "\n" 标示结束符，客户端自己处理
            result = result + "\n";
            System.out.println("客户端传来消息：" + result);
            // 给客户端返回数据
            outputStream.write(result.getBytes(StandardCharsets.UTF_8));

            outputStream.flush();
            System.out.println("传递客户端消息结束");

            inputStream.close();
            outputStream.close();
            socket.close();
        }

    }
}

```



```shell
root@qinjp-Virtual-Machine:/usr/local/java# strace -ff -o log java BioServer.java
服务启动完成
打开新窗口

root@qinjp-Virtual-Machine:/usr/local/java# ll
total 183748
drwxr-xr-x  3 root root      4096  7月 26 12:34 ./
drwxr-xr-x 11 root root      4096  7月 26 11:18 ../
-rw-r--r--  1 root root      1639  7月 26 12:24 BioServer.java
-rw-r--r--  1 root root       250  7月 26 11:45 BolckDemo.java
drwxr-xr-x  8 root root      4096  7月 26 11:27 jdk-17/
-rw-r--r--  1 root root     15614  7月 26 12:34 log.1757
-rw-r--r--  1 root root    247535  7月 26 12:34 log.1758
-rw-r--r--  1 root root      9171  7月 26 12:34 log.1759
-rw-r--r--  1 root root      1413  7月 26 12:34 log.1760
-rw-r--r--  1 root root      1074  7月 26 12:34 log.1761
-rw-r--r--  1 root root      1110  7月 26 12:34 log.1762
-rw-r--r--  1 root root      1641  7月 26 12:34 log.1763
-rw-r--r--  1 root root      2693  7月 26 12:34 log.1764
-rw-r--r--  1 root root    243521  7月 26 12:34 log.1765
-rw-r--r--  1 root root    876560  7月 26 12:34 log.1766
-rw-r--r--  1 root root       916  7月 26 12:34 log.1767
-rw-r--r--  1 root root      1077  7月 26 12:34 log.1768
-rw-r--r--  1 root root       909  7月 26 12:34 log.1769
-rw-r--r--  1 root root     32654  7月 26 12:34 log.1770

一般最大的就是Java main方法的线程
root@qinjp-Virtual-Machine:/usr/local/java# less log.1758
关键代码
socket(AF_INET6, SOCK_STREAM, IPPROTO_IP) = 4
bind(4, {sa_family=AF_INET6, sin6_port=htons(8080), sin6_flowinfo=htonl(0), inet_pton(AF_INET6, "::", &sin6_addr), sin6_scope_id=0}, 28) = 0
listen(4, 50)                           = 0
accept(4,

```



socket 方法标示创建一个ServerSocket  ，返回一个 等于4的FD 

bind 绑定4号FD到8080

listen 对4号FD的监听 ，队列长度为 50

accept  接收一个socket连接



```shell
man listen

listen(int sockfd, int backlog);
```

参数：backlog 标示服务器可连接的队列个数

>Linux 内核中TCP连接维护两个队列，SYN queue 队列 和 Accept queue 队列 
>
>SYN queue 队列 ：3次握手没有完成的
>
>Accept queue 队列 ：3次握手完成的
>
>backlog：标示这两个队列之和，超过这个数，新的TCP连接就会被拒绝
>
>注意如果连接处于休眠状态即没有数据传输是不属于服务处理中的连接，所以不会计算在内



对应 Java backlog，Netty ChannelOption.SO_BACKLOG,128

```java
java：
ServerSocket serverSocket = new ServerSocket(8080, 50);
public ServerSocket(int port, int backlog) throws IOException {
    this(port, backlog, null);
}
netty：
ServerBootstrap serverBootstrap = new ServerBootstrap();
serverBootstrap.option(ChannelOption.SO_BACKLOG,128);
```



#### 给服务器发生消息

nc loaclhost 8080

```shell
root@qinjp-Virtual-Machine:~# nc localhost 8080
asdasda



```

回到之前的less窗口shift

```shell
root@qinjp-Virtual-Machine:/usr/local/java# less log.1758

接收到 6 号FD
accept(4, {sa_family=AF_INET6, sin6_port=htons(53894), sin6_flowinfo=htonl(0), inet_pton(AF_INET6, "::ffff:127.0.0.1", &sin6_addr), sin6_scope_id=0}, [28]) = 6
读取6号FD内容,这里阻塞了，对应Java的read
read(6, 



接收到 asdasda
read(6, "asdasda\n", 1024)              = 8
接着等待新的输入
read(6, 

read(6, "", 1024)                       = 0
断开连接后回写客户端
write(6, "\346\224\266\345\210\260\346\225\260\346\215\256\357\274\232asdasda\n\n", 24) = 24
关闭连接
shutdown(6, SHUT_WR)                    = -1 ENOTCONN (Transport endpoint is not connected)
close(6)                                = 0

4号FD等待新socket连接
accept(4, 

```





#### 系统层解决BIO阻塞的问题

通过上面的分析，BIO的阻塞点在 accept 函数 和  read 函数

操作系统提供的非阻塞的方式

```shell
fcntl(4, F_SETFL, O_RDWR|O_NONBLOCK)    = 0
```



### NIO系统调用

#### Java 代码

```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class NioServer {

    public static void main(String[] args) throws IOException {
        // 绑定端口，开启服务
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));

        final Selector selector = Selector.open();
        // 服务端的serverSocketChannel注册到 selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务器启动成功");

        while (true) {
            // 阻塞等待客户端事件发送，这里有超时时间设置
            int select = selector.select();
            if (select < 1) {
                System.out.println("当前没有连接进来");
            }
            // 注册上了的 channel 都对应一个 SelectionKey
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // selector多路复用器接收到一个accept事件
                if (key.isAcceptable()) {
                    // 接受请求
//                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
//                    SocketChannel newSocketChannel = serverSocketChannel.accept();
                    // 这里会接收一个客户端SocketChannel的连接请求，并返回对应的SocketChannel
                    // 注意这里如果没有对应的客户端Channel就会返回null
                    SocketChannel newSocketChannel = serverSocketChannel.accept();
                    System.out.println("收到客户端请求：" + newSocketChannel.getRemoteAddress());
                    // 每一个新的客户端请求都设置成非阻塞
                    newSocketChannel.configureBlocking(false);
                    // 将与客户端对接好的newSocketChannel注册到select上，并关注读事件
                    // 注册读事件，需要绑定一个buffer相当于附件，所有的事件交互都通过这个buffer
                    newSocketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                // 数据读取事件
                if (key.isReadable()) {
                    // 其他的代码基本都是这个模板，只是这个处理客户端请求需要定制
                    // accept事件是ServerSocketChannel
                    // read事件是SocketChannel
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    // 取上次accept注册的buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    buffer.clear();
                    socketChannel.read(buffer);
                    String request = new String(buffer.array(), StandardCharsets.UTF_8);
                    System.out.println("收到客户端消息：" + request);

                    // 回写
                    String str = "服务端收到消息：" + request;
                    socketChannel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));

                }
                // 已经处理完的事件要清除，防止重复处理
                // 不然的话，对应连接请求，服务端还是会去accept产生一个SocketChannel
                // 但此时客户端没有开对接，就会返回一个null
                // selector设计的就是如此：
                // selector.select()是将所有准备好的channel以SelectionKey的形式放置于selector的selectedKeys()中供使用者迭代，用的过程中需将selectedKeys清空
                // selector不会自己从已选择集合中移除selectionKey实例,不人工remove()，selector会认为该感兴趣的事件没有被处理
                // 人工remove()是告诉selector该channel的感兴趣的事件已经处理好了
                iterator.remove();

            }

        }
    }
}

```



####  查看NIO系统调用指令

```shell

root@qinjp-Virtual-Machine:/usr/local/java# strace -ff -o log java NioServer.java
服务器启动成功



```



```shell

新窗口
less log.1758
关键代码
# socket 4 
socket(AF_INET6, SOCK_STREAM, IPPROTO_IP) = 4
# 设置 4号socket为非阻塞: 对应java : serverSocketChannel.configureBlocking(false);
fcntl(4, F_SETFL, O_RDWR|O_NONBLOCK)    = 0
# 绑定4号到端口8080
bind(4, {sa_family=AF_INET6, sin6_port=htons(8080), sin6_flowinfo=htonl(0), inet_pton(AF_INET6, "::", &sin6_addr), sin6_scope_id=0}, 28
) = 0
# 监听4号socket，最大50个连接
listen(4, 50) 
# 创建一个多路复用器 就是 select: 对应java :  Selector selector = Selector.open();
epoll_create1(EPOLL_CLOEXEC)            = 6
# 4号socket的回调函数注册到 selector上,事件为EPOLL_CTL_ADD 
# 对应java: serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
epoll_ctl(6, EPOLL_CTL_ADD, 4, {events=EPOLLIN, data={u32=4, u64=140694538682372}}) = 0
# 多路复用器等待客户端产生输入事件 对应java : int select = selector.select();
epoll_wait(6,





```



```shell

新窗口
root@qinjp-Virtual-Machine:/usr/local/java# nc localhost 8080

less log.1758
# 接上面的 多路复用器等待客户端产生输入事件
epoll_wait(6, [{events=EPOLLIN, data={u32=4, u64=140694538682372}}], 1024, -1) = 1
# 8号 nc 客户端新的连接  SocketChannel newSocketChannel = serverSocketChannel.accept();
accept(4, {sa_family=AF_INET6, sin6_port=htons(44850), sin6_flowinfo=htonl(0), inet_pton(AF_INET6, "::ffff:127.0.0.1", &sin6_addr), sin6_scope_id=0}, [28]) = 8
# 8号新连接的回调函数：注册到6号多路复用器中   newSocketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
epoll_ctl(6, EPOLL_CTL_ADD, 8, {events=EPOLLIN, data={u32=8, u64=140694538682376}}) = 0
# 8号新连接设置成非阻塞   newSocketChannel.configureBlocking(false);
fcntl(8, F_SETFL, O_RDWR|O_NONBLOCK)    = 0
# 读到8号连接客户发送的数据 socketChannel.read(buffer);
read(8, "12345\n", 1024)                = 6
# 回写
write(8, "\346\234\215\345\212\241\347\253\257\346\224\266\345\210\260\346\266\210\346\201\257\357\274\23212345\n\0\0"..., 1048) = 1048

# 6号多路复用器等待新的客户端产生输入事件
epoll_wait(6,


```











### 代码

```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class NioServer {

    public static void main(String[] args) throws IOException {
        // 绑定端口，开启服务
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));

        final Selector selector = Selector.open();
        // 服务端的serverSocketChannel注册到 selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务器启动成功");

        while (true) {
            // 阻塞等待客户端事件发送，这里有超时时间设置
            int select = selector.select();
            if (select < 1) {
                System.out.println("当前没有连接进来");
            }
            // 注册上了的 channel 都对应一个 SelectionKey
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // selector多路复用器接收到一个accept事件
                if (key.isAcceptable()) {
                    // 接受请求
//                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
//                    SocketChannel newSocketChannel = serverSocketChannel.accept();
                    // 这里会接收一个客户端SocketChannel的连接请求，并返回对应的SocketChannel
                    // 注意这里如果没有对应的客户端Channel就会返回null
                    SocketChannel newSocketChannel = serverSocketChannel.accept();
                    System.out.println("收到客户端请求：" + newSocketChannel.getRemoteAddress());
                    // 每一个新的客户端请求都设置成非阻塞
                    newSocketChannel.configureBlocking(false);
                    // 将与客户端对接好的newSocketChannel注册到select上，并关注读事件
                    // 注册读事件，需要绑定一个buffer相当于附件，所有的事件交互都通过这个buffer
                    newSocketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                // 数据读取事件
                if (key.isReadable()) {
                    // 其他的代码基本都是这个模板，只是这个处理客户端请求需要定制
                    // accept事件是ServerSocketChannel
                    // read事件是SocketChannel
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    // 取上次accept注册的buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    buffer.clear();
                    socketChannel.read(buffer);
                    String request = new String(buffer.array(), StandardCharsets.UTF_8);
                    System.out.println("收到客户端消息：" + request);

                    // 回写
                    String str = "服务端收到消息：" + request;
                    socketChannel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));

                }
                // 已经处理完的事件要清除，防止重复处理
                // 不然的话，对应连接请求，服务端还是会去accept产生一个SocketChannel
                // 但此时客户端没有开对接，就会返回一个null
                // selector设计的就是如此：
                // selector.select()是将所有准备好的channel以SelectionKey的形式放置于selector的selectedKeys()中供使用者迭代，用的过程中需将selectedKeys清空
                // selector不会自己从已选择集合中移除selectionKey实例,不人工remove()，selector会认为该感兴趣的事件没有被处理
                // 人工remove()是告诉selector该channel的感兴趣的事件已经处理好了
                iterator.remove();

            }

        }


    }


}

```

