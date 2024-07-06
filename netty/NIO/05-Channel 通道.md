### Channel 通道

Channel 是一种用于IO操作的连接。一个Channel代表一个与外部设备的开放连接。

这些设备包括硬件设备，一个文件，一个网络的Socket或者一个支持同时提供一个或者多个不同操作的IO操作组件。

Channel有两种状态，open和close。创建的时候是open状态，一旦关闭就一直保持close状态，对于close状态的Channel

所有IO操作都会拋ClosedChannelException异常。

一般来说Channel以及他的子接口和子类，都要设计成多线程安全



### Channel 和 传统的Stream 的区别

1. Channel 可以异步读写，Stream 不支持异步
2. Channel 可以读也可以写 双向数据传输，Stream 只能单向访问
3. Channel 必须结合 Buffer来读写，Stream 可以直接访问目标数据
4. Channel 相比 Stream 性能更好



### Channel 的几个重要实现 

NetworkChannel 主要针对网络的

FileChannel 主要针对文件的



### NetworkChannel 

NetworkChannel 是一个针对网络的接口：主要支持TCP和UDP两种协议

TCP协议主要有：ServerSocketChannel 和 SocketChannel

UDP协议主要有：DatagramChannel

UDP代码：

```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public class UDPServer {

    public static void main(String[] args) throws IOException {

        DatagramChannel channel = DatagramChannel.open();

        InetSocketAddress local = new InetSocketAddress(9999);
        //绑定
        channel.bind(local);
        //buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //接收
        while (true) {
            System.out.println("==========================");
            buffer.clear();
            SocketAddress socketAddress = channel.receive(buffer);
            System.out.println(socketAddress.toString());
            System.out.println(new String(buffer.array(), StandardCharsets.UTF_8));
        }

    }
}

```

```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public class UDPClient {
    public static void main(String[] args) throws IOException {
        DatagramChannel channel =DatagramChannel.open();
        InetSocketAddress remote = new InetSocketAddress("localhost",9999);
        //channel.connect(remote);

        String msg = "hello UDP server ,I am qinjp ," + System.currentTimeMillis();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(msg.getBytes(StandardCharsets.UTF_8));

        // 由写入读
        buffer.flip();

        //channel.write(buffer);
        int send = channel.send(buffer, remote);

        System.out.println("已发送完成:" + send);

        channel.close();

    }

}

```



```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class UDPSelectServer {

    public static void main(String[] args) throws IOException {

        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        //如果在两台物理计算机中进行实验，则要把localhost改成服务端的IP地址
        channel.bind(new InetSocketAddress("localhost", 9999));
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("UDPSelectServer start ");
        //  阻塞等待客户端事件发送，这里有超时时间设置
        while (selector.select() > 0) {
            System.out.println("有数据发送过来");
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectionKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isReadable()) {
                    channel = (DatagramChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    channel.receive(byteBuffer);
                    System.out.println(new String(byteBuffer.array(), 0, byteBuffer.position()));
                }
                it.remove();
            }
            System.out.println("接收完成");
        }
        channel.close();

    }
}

```

```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class UDPSelectClient {

    public static void main(String[] args) throws IOException {
        InetSocketAddress address = new InetSocketAddress("localhost", 9999);
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(address);

        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_WRITE);
        selector.select();
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> it = selectionKeys.iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            if (key.isWritable()) {
                ByteBuffer byteBuffer = ByteBuffer.wrap("我来自客户端！".getBytes());
                channel.send(byteBuffer,address);
//                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//                byteBuffer.put("我来自客户端！".getBytes(StandardCharsets.UTF_8));
//                byteBuffer.flip();
//                channel.write(byteBuffer);
            }
        }
        channel.close();

        System.out.println("client end!");


    }
}

```





### Channel 如何保证可靠性

**通过fsync 函数保证可靠性**

下面的代码：在强制关机下text.txt的内容可能丢失

```java
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDemo {

    public static void main(String[] args) throws IOException {
        File file = new File("/usr/local/java/text.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < 10; i++) {
            out.write('a');
            out.flush();
        }
        out.close();
    }
}
```

```shell
root@qinjp-Virtual-Machine:/usr/local/java# strace -ff -o file  java FileDemo.java

root@qinjp-Virtual-Machine:/usr/local/java# less file.1356

关键代码：没有fsync 函数
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
write(4, "a", 1)                        = 1
pread64(3, "\312\376\272\276\0\0\0=\0(\t\0\2\0\3\7\0\4\f\0\5\0\6\1\0\32java/i"..., 736, 8497272) = 736
close(4)  


```



Channel 使用

```java
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class FileDemo1 {

    public static void main(String[] args) throws IOException {
        File file = new File("text1.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
        FileChannel fc = randomAccessFile.getChannel();
        fc.map(FileChannel.MapMode.READ_WRITE,0,10);
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("qinjiepeng".getBytes(StandardCharsets.UTF_8));
        System.out.println(buffer.toString());
        buffer.flip();
        fc.position(0).write(buffer);
        fc.force(true);
        fc.close();
        randomAccessFile.close();

    }

}

```



```shell
root@qinjp-Virtual-Machine:/usr/local/java# strace -ff -o file  java FileDemo1.java

root@qinjp-Virtual-Machine:/usr/local/java# less file.1617

关键代码：有fsync 函数
write(4, "qinjiepeng", 10)              = 10
fsync(4)                                = 0
close(4)                                = 0
```



### fsync 函数

将 Page Cache 刷入硬盘





### Page Cache 页高速缓冲存储器

操作系统内核级别的缓存机制。

当CPU要访问磁盘文件时，需要将磁盘的文件拷贝到内存中缓存起来，以加快处理。但内存有限，遇到大文件就无法缓存了

Linux 以4K大小为一个内存块，称为页Page 

在Linux上打开文件，会以4K为单位一次将所有的文件都加载到Page Cache中。

这样以后每次打开文件都不需要去硬盘中找了

查看页缓存命令：cat /proc/meminfo

```shell
root@qinjp-Virtual-Machine:/proc# cat /proc/meminfo

MemTotal:        3157352 kB
.....
Cached:           532872 kB
.....
Dirty:                 0 kB
.....
Mapped:           232396 kB
.....

 
```



使用 pcstat 查看某个文件的 page cache 

https://github.com/tobert/pcstat



CPU 运行速度非常快，不适合直接操作磁盘，网卡这样的硬件

因此写文件的时候，也是先写入Page Cache中，缓存起来，然后再往硬件写入

这时候就带来了一些问题

Page Cache 页缓存写入和写入硬盘中是有时间差的

应用程序写入了，less ，cat 这些命令也看到了。

但实际上，文件只是在Page Cache中，并没有真正写入硬盘

应用程序很难感知，这个是应用程序需要面对的问题



正常情况下，操作系统有稳定的机制，正常关机的时候，操作系统会统一将Page Cache写入硬件

而且在运行中，对于有数据修改的page页，操作系统会标记为脏页（Dirty Page），当脏页到达一定的比例

操作系统就会触页缓存的写入操作，这些应用程序都不需要参与



考虑到一些高可用的场景，防止一断电数据就丢失

内核提供了 fsync 函数 强制页缓存写入机制



这里引出一个大难题：如何应用程序都无法保证数据100%不丢失

1. 为了性能，不可能每次都写入都 fsync ，性能消耗无法接受
2. 不能保证每次执行fsync都能成功，调用fsync到CPU执行，这中间还是有时间差的

所以综合考虑，平衡个方面，寻合适的方案，没有最好的方案





### DMA 直接存储器访问

**简单理解：CPU的秘书，帮助CPU减轻负担**

应用程序与磁盘之间的数据写入和写出，都需要在用户态和内核态之间来回直接复制数据

内核态中的数据通过调用系统底层的IO接口，完成与磁盘的数据存储

（内核态）调用系统底层的IO接口，早期都是由CPU独立负责。所以大规模读写请求时，CPU占用率很高

![](img\2022-08-18 171416.png)



为了解决CPU被各种IO接口占用，引人了DMA（直接存储器存储）

应用程序对操作系统发出一个读写请求时，会由DMA先向CPU申请权限，之后内存空间（Page Cache）与磁盘之间的IO操作全部由DMA负责

这样CPU在读写过程中就不需要再参与，减少CPU的负担

![](img\2022-08-18 172445.png)



到这里还有一个问题：内存空间和硬盘之间的数据传输还需要借助数据总线，CPU 对某个设备接口响应 DMA 请求时，会让出总线控制权

当数据总线过多时，大量的IO操作会造成总线冲突，进而影响其性能

为了解决总线冲突造成的性能影响，就有了Channel通道的方式。

Channel通道的方式：通道有自己的io控制和指令系统，并且有专门通讯传输的通道总线

Channel通道：实质是一台能够执行有限输入输出指令的CPU，并且有专门通讯传输的通道总线

简单理解：通道则是在 DMA 的基础上增加了能执行有限通道指令的 I/O 控制器，并且有专门通讯传输的通道总线，代替CPU管理控制外设

DMA只是一个低能的硬件电路，只能实现固定的数据传送控制

![](img\2022-08-18 181124.png)















 ### 零拷贝

零拷贝：避免CPU将一块存储拷贝到另外一块存储的技术

Java 中两种实现形式，都是基于FileChannel，一种nmap文件映射，一种sendfile的方式

```java
   /* 
	* 基于nmap文件映射
	*/
    public static void mappedByteBuffer() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("a.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();
        /**
         * 参数1：FileChannel.MapMode.READ_WRITE 使用读写模式
         * 参数2：内存起始位置
         * 参数3：映射内存的大小,即将a.txt的多少个字节映射到内存中
         * 超过大小，会报错 IndexOutOfBoundsException
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0, "Q".getBytes());
        mappedByteBuffer.put(1, "J".getBytes());
        mappedByteBuffer.put(2, "P".getBytes());
        // 报错 IndexOutOfBoundsException
        // mappedByteBuffer.put(5, "P".getBytes());
        randomAccessFile.close();
        System.out.println("修改结束");

    }
   /* 
	* 基于sendfile的方式
	*/
    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
```





### 零拷贝是怎么回事

我们知道零拷贝是操作系统底层技术，程序只能调用，无法实现零拷贝，和多路复用一样。

典型场景：一个文件下载的服务端应用程序

本地读取磁盘文件，然后通过socket连接，发个客户端

这个场景下：

1. 操作系统首先需要将文件读取到内核态的页缓存中（一次DMA复制）
2. 接着将页缓存的数据加载到用户态的应用程序中（一次CPU复制）
3. 然后服务端的应用程序将数据写入内核态的Socket缓冲区（一次CPU复制）
4. 最后通过Socket将内核态的Socket缓冲区的数据发送给客户端（一次DMA复制）

整个过程4次文件拷贝

![](img\2022-08-18 221246.png)

零拷贝主要任务就是避免CPU的参与拷贝，这个过程中，硬件和页缓存之间已经通过底层DMA或者Channel进行，CPU没有参与

所以重点就是减少内核态和用户态之间的文件拷贝



#### nmap方式

用户态不在保存文件内容，而只保存文件的映射。

这里还有一个缺点：页缓存和Socket缓存区还是CPU拷贝

Java实现方式：FileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);  

![](img\2022-08-18 222346.png)





#### sendfile系统调用实现方式

系统通过sendfile函数调用，可以再内核层中完成文件拷贝

Java实现方式：FileChannel.transferTo(long position, long count,WritableByteChannel target);

sendfile工作原理：

在文件拷贝过程中，不拷贝文件内容，只拷贝文件信息的文件描述符FD到socket缓存区

而真正的文件内容，则交由DMA控制器直接从文件页缓存打包发送到socket客户端

整个过程CPU没有参与

![](img\2022-08-18 223736.png)



#### 早期 sendfile 

sendfile 早期是为了解决mmap的一些问题简化操作

并且描述符`in_fd`必须是nmap的FD，描述符`out_fd`必须指向一个套接字，只能在socket中使用

sendfile 早期减少了数据拷贝的次数，还减少了上下文切换，数据传送始终只发生在内核态中

nmap 需要用户态和内核态之间进行映射，sendfile 减少这一步，但页缓存和Socket缓存区还是用CPU拷贝

![](img\2022-08-18 223737.png)

后面出现了DMA引擎仅需要把缓冲区描述符传到`socket`缓冲区，这样`DMA`控制器直接将页缓存中的数据打包发送到网络中

![](img\2022-08-18 223738.png)

2.6.3版本之后sendfile 的`out_fd`必须指向一个套接字的限制也被取消



### 总结零拷贝

nmap方式有将文件拷贝到应用程序中，还需要用户态参与，所以文件映射不宜过大

sendfile 方式纯内核态的操作，所以文件大小不限制，但还需用户态去调度，用户态转内核态通知调度，性能开销要考虑

因此sendfile 适合大文件传输

























































