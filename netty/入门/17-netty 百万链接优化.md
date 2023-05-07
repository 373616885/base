### 单机百万连接调优

如何模拟百万连接



突破局部文件句柄的限制（单进程的限制）



突破全部文件句柄的限制（系统的限制）





### 如何模拟百万连接

通常情况下

服务端一个端口 8080 



客户端端口只能 1025--65535



服务端一个端口 其中只能有 6w 连接



如何模拟呢？

既然一个服务端口只能有 6w 连接，那么可以多开几个服务端的端口



服务端端口 8000 -- 8100

服务端口能有 6w 连接 ，100 个服务端端口能有 600w个连接



![](img\2022-09-12 063247.png)







 ### demo

Client 

```java
package com.qin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Client {

    public static void main(String[] args) throws InterruptedException {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        //客户端
        var bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(EmptyHandler.INSTANCE);


        //发起异步连接操作，同步阻等待结果
        var port = 8000;
        while (!Thread.interrupted()) {
            bootstrap.connect("127.0.0.1", port++)
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            log.warn("connect failed exit!");
                            System.exit(0);
                        }
                    }).sync();
            if (port > 8099) {
                port = 8000;
            }
        }


    }


    @ChannelHandler.Sharable
    private static class EmptyHandler extends ChannelInboundHandlerAdapter {

        static final EmptyHandler INSTANCE = new EmptyHandler();

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.warn("connect success :" + ctx.channel().remoteAddress());
        }
    }


}

```



 Server

```java
package com.qin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 */
@Slf4j
public class Server {

    @SneakyThrows
    public static void main(String[] args) {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        // NioEventLoopGroup是一个线程池
        // bossGroup是否可以指定多个呢，可以是可以，但是没有用， 即使指定100，netty最终也只会创建1个。
        // 处理连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("bossGroup"));
        // 处理处理Channel（通道）的I/O事件。要是 2的次数幂，才是最优解
        EventLoopGroup workGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("workGroup"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workGroup);
        // 指明使用NIO进行网络通讯
        serverBootstrap.channel(NioServerSocketChannel.class);
        // 设置TCP连接个数--完成握手和没完成握手队列之和 ，默认是50
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);

        serverBootstrap.childHandler(ConnectCountHandler.INSTANCE);


        for (var i = 0; i < 100; i++) {
            final int port = 8000 + i;
            serverBootstrap.bind(port).addListener(future -> {
                if (future.isSuccess()) {
                    log.warn("server start success port:" + port);
                }
            }).sync();//等待bind完成
        }

        // finally
        //bossGroup.shutdownGracefully();
        //workGroup.shutdownGracefully();
        // 服务端不需要向客户端一样，main方法线程可以结束
        // 客户端线程main线程结束了，connect也就结束了

    }

    private static final AtomicInteger COUNT = new AtomicInteger();

    @ChannelHandler.Sharable
    private static class ConnectCountHandler extends ChannelInboundHandlerAdapter {

        private ConnectCountHandler() {
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                log.warn("connects: " + COUNT.get());
            }, 1, 2, TimeUnit.SECONDS);
        }

        static final ConnectCountHandler INSTANCE = new ConnectCountHandler();

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            COUNT.incrementAndGet();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            //log.warn("异常终止: " + cause.getMessage());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            COUNT.decrementAndGet();
        }
    }


}

```

打包插件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.qin</groupId>
    <artifactId>optimization</artifactId>
    <version>1.0</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>

        <!-- https://mvnrepository.com/artifact/io.netty/netty-all -->
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-all</artifactId>
            <version>4.1.78.Final</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/cn.hutool/hutool-all -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.5</version>
        </dependency>

        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <version>2.17.2</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.17.2</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-jul</artifactId>
            <version>2.17.2</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>jul-to-slf4j</artifactId>
            <version>1.7.36</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.24</version>
        </dependency>

        <!-- log4j2 异步日志 -->
        <dependency>
            <groupId>com.lmax</groupId>
            <artifactId>disruptor</artifactId>
            <version>3.4.4</version>
        </dependency>
        <!-- log4j2 end-->

    </dependencies>


    <repositories>
        <repository>
            <id>alimaven</id>
            <name>aliyun maven</name>
            <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
        </repository>

        <repository>
            <id>spring-snapshots</id>
            <url>http://repo.spring.io/libs-snapshot</url>
        </repository>
    </repositories>


    <pluginRepositories>
        <pluginRepository>
            <id>spring-snapshots</id>
            <url>http://repo.spring.io/libs-snapshot</url>
        </pluginRepository>
    </pluginRepositories>


    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <!-- java编译插件 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <!-- 把依赖也打进jar包：mainClass是jar包的main方法入口 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.4.2</version>
                <configuration>
                    <!-- get all project dependencies -->
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <!-- 配置执行器 -->
                    <execution>
                        <id>make-assembly-client</id>
                        <!-- 绑定到package命令的生命周期上 -->
                        <configuration>
                            <!-- MainClass in mainfest make a executable jar -->
                            <archive>
                                <manifest>
                                    <mainClass>com.qin.Client</mainClass>
                                </manifest>
                            </archive>
                            <finalName>${project.artifactId}-client</finalName>
                        </configuration>
                        <phase>package</phase>
                        <goals>
                            <!-- 只运行一次 -->
                            <goal>single</goal>
                        </goals>
                    </execution>
                    <!-- 配置执行器 -->
                    <execution>
                        <id>make-assembly-server</id>
                        <!-- 绑定到package命令的生命周期上 -->
                        <configuration>
                            <!-- MainClass in mainfest make a executable jar -->
                            <archive>
                                <manifest>
                                    <mainClass>com.qin.Server</mainClass>
                                </manifest>
                            </archive>
                            <finalName>${project.artifactId}-server</finalName>
                        </configuration>
                        <phase>package</phase>
                        <goals>
                            <!-- 只运行一次 -->
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>


</project>
```





### 突破局部文件句柄限制

查看所有的资源限制

```shell
ulimit -a
```

![](img\55.PNG)



```shell
-a：显示目前资源限制的设定；
-c <core文件上限>：设定core文件的最大值，单位为区块；
-d <数据节区大小>：程序数据节区的最大值，单位为KB；
-f <文件大小>：shell所能建立的最大文件，单位为区块；
-H：设定资源的硬性限制，也就是管理员所设下的限制；
-m <内存大小>：指定可使用内存的上限，单位为KB；
-n <文件数目>：指定同一时间最多可开启的文件数；
-p <缓冲区大小>：指定管道缓冲区的大小，单位512字节；
-s <堆叠大小>：指定堆叠的上限，单位为KB；
-S：设定资源的弹性限制；
-t <CPU时间>：指定CPU使用时间的上限，单位为秒；
-u <程序数目>：用户最多可开启的程序数目；
-v <虚拟内存大小>：指定可使用的虚拟内存上限，单位为KB。
```

同时开启的文件数

```shell
ulimit -n
```

当前用户下最多可以运行多少进程或线程

```shell
ulimit -u
```

查看默认的线程栈大小，单位是字节（Bytes）

```shell
ulimit -s
```



limits.conf文件限制着用户可以使用的最大文件数，最大线程，最大内存等资源使用量

```shell
cat /etc/security/limits.conf
```

在文件尾添加

```shell
* soft nofile 65535  #任何用户可以打开的最大的文件描述符数量，默认1024，这里的数值会限制tcp连接
* hard nofile 65535
* soft nproc  65535  #任何用户可以打开的最大进程数
* hard nproc  65535
```



重启服务器 limits.conf 生效

临时解决办法：

```shell
ulimit -n 65535
```



### 突破全局文件句柄限制

```shell
cat /proc/sys/fs/file-max
```

临时解决办法


```shell
echo 1048576 > /proc/sys/fs/file-max
```

永久解决办法

```shell
cat /etc/sysctl.conf

vim /etc/sysctl.conf
最后添加
fs.file-max=1048576
刷新生效
sysctl -p
```





### 应用级别的调优

**业务channelRead用另外的线程池去操作**

瓶颈在于：业务channelRead阻塞了IO线程

两种方式：

```java
//第一种：
private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(30, new DefaultThreadFactory("bizGroup"));
//自己写一个线程池：去执行业务handler
@Override
protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
    // 业务转换ByteBuf
    EXECUTOR.submit(() -> {
        msg.retain();
        getResult();
        ctx.writeAndFlush(msg);
    });
}

//第二种：
//另外定义一个bizGroup
EventLoopGroup bizGroup = new NioEventLoopGroup(30, new DefaultThreadFactory("bizGroup"));

//添加ServerBizHandler的时候用bizGroup
ch.pipeline().addLast(bizGroup,ServerBizHandler.INSTANCE);

```



### demo

Server

```java
package com.qin.two;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Administrator
 */
@Slf4j
public class Server {

    @SneakyThrows
    public static void main(String[] args) {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        // NioEventLoopGroup是一个线程池
        // bossGroup是否可以指定多个呢，可以是可以，但是没有用， 即使指定100，netty最终也只会创建1个。
        // 处理连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("bossGroup"));
        // 处理处理Channel（通道）的I/O事件。要是 2的次数幂，才是最优解
        EventLoopGroup workGroup = new NioEventLoopGroup(new DefaultThreadFactory("workGroup"));

        //EventLoopGroup bizGroup = new NioEventLoopGroup(30, new DefaultThreadFactory("bizGroup"));

        var serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workGroup);
        // 指明使用NIO进行网络通讯
        serverBootstrap.channel(NioServerSocketChannel.class);
        // 设置TCP连接个数--完成握手和没完成握手队列之和 ，默认是50
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);

        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                ch.pipeline().addLast(ServerBizHandler.INSTANCE);
                //ch.pipeline().addLast(bizGroup, ServerBizHandler.INSTANCE);
            }
        });


        var channelFuture = serverBootstrap.bind(8080).addListener(future -> {
            if (future.isSuccess()) {
                log.warn("server start success");
            }
        }).sync();//等待bind完成

        //阻塞在此
        channelFuture.channel().closeFuture().sync();

        // finally
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();

    }


    @ChannelHandler.Sharable
    private static class ServerBizHandler extends SimpleChannelInboundHandler<ByteBuf> {

        static final ServerBizHandler INSTANCE = new ServerBizHandler();

        private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(30, new DefaultThreadFactory("bizGroup"));

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            // 业务转换ByteBuf
            EXECUTOR.submit(() -> {
                msg.retain();
                getResult();
                ctx.writeAndFlush(msg);
            });
        }

        @SneakyThrows
        private void getResult() {
            // 90% 1ms
            // 95 % 10ms
            // 99% 100ms
            // 99.99% 1000ms

            var level = ThreadLocalRandom.current().nextInt(1000);

            var time = 1000;

            if (level <= 900) {
                time = 50;
            } else if (level <= 950) {
                time = 100;
            } else if (level <= 990) {
                time = 500;
            }


            Thread.sleep(time);
        }
    }


}
```



Client

```java
package com.qin.two;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Client {

    public static void main(String[] args) throws InterruptedException {

        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);

        //客户端
        var bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                        ch.pipeline().addLast(ClientBizHandler.INSTANCE);
                    }
                });

        // 1000个连接每个连接1秒发送一个请求
        for (int i = 0; i < 1000; i++) {
            bootstrap.connect("127.0.0.1", 8080)
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            log.warn("connect failed exit!");
                            System.exit(0);
                        }
                    }).sync();
        }

    }


    @ChannelHandler.Sharable
    static class ClientBizHandler extends SimpleChannelInboundHandler<ByteBuf> {

        static final ClientBizHandler INSTANCE = new ClientBizHandler();

        private static final AtomicLong TOTAL_RESPONSE_TIME = new AtomicLong();
        private static final AtomicLong TOTAL_RESPONSE_COUNT = new AtomicLong();
        private static final AtomicLong START_TIME = new AtomicLong();

        private static final FastThreadLocalThread THREAD = new FastThreadLocalThread(() -> {
            while (true) {
                long duration = System.currentTimeMillis() - START_TIME.get();
                if (duration > 0) {
                    long count = TOTAL_RESPONSE_COUNT.get();
                    final long total_time = TOTAL_RESPONSE_TIME.get();
                    float qps = 1000 * count / duration;
                    float avgTime = total_time / count;
                    System.out.println("qps:" + qps + " avg:" + avgTime + " count:" + count + " total_time:" + total_time);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "qps");


        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            log.warn("connect success :" + ctx.channel().remoteAddress());
            var byteBuf = Unpooled.directBuffer(Long.BYTES);//重用非池的;
            ctx.executor().scheduleWithFixedDelay(() -> {
                byteBuf.retain();
                var data = System.currentTimeMillis();
                byteBuf.writeLong(data);
                ctx.writeAndFlush(byteBuf);
                byteBuf.clear();
            }, 1, 2, TimeUnit.SECONDS);
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            TOTAL_RESPONSE_TIME.addAndGet(System.currentTimeMillis() - msg.readLong());
            TOTAL_RESPONSE_COUNT.incrementAndGet();
            if (START_TIME.compareAndSet(0, System.currentTimeMillis())) {
                THREAD.start();
            }
        }
    }


}
```









