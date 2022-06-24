### Socket 通信例子

#### 服务端

```java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;

    public Server(Integer port) {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("服务器启动成功------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // 这里new一个线程，不影响主线程，可以去掉
        new Thread(() -> {
            doStart();
        }).start();
    }

    public void doStart(){
        while (true) {
            try {
                // 这里会阻塞-等待新的客户端进来
                Socket client = serverSocket.accept();
                System.out.println("新的连接------" + client.getRemoteSocketAddress()) ;
                new HandlerClient(client).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        Server server = new Server(8888);
        server.start();
    }
}


import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 服务端处理客户端信息
 */
public class HandlerClient {
    private static final int MAX_DATA_LEN = 1024;

    private final Socket client;

    public HandlerClient(Socket client) {
        this.client = client;
    }

    public void start() {
        new Thread(() -> {
            // 这里要创建一个新线程
            // 因为客户端的读写会阻塞影响到服务端的accept
            handler();
        }).start();
    }

    private void handler() {
        try {
            InputStream inputStream = client.getInputStream();
            while (true) {
                byte[] date = new byte[MAX_DATA_LEN];
                int len;
                // inputStream.read 会阻塞等待再次输入
                while ((len = inputStream.read(date)) != -1) {
                    String message = new String(date, 0, len);
                    System.out.println("客户端传来消息：" + message);
                    // 返回客户端数据  "\n" 标示结束符，客户端自己处理
                    String result = "收到数据：" + message + "\n";
                    // 给客户端返回数据
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(result.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```



#### 客户端

```java
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {

    private static final String HOST = "127.0.0.1";

    private static final Integer PORT = 8888;

    private static final int SLEEP_TIME = 5000;

    private static int i = 1;

    public static void main(String[] args) throws IOException {

        Socket client = new Socket(HOST, PORT);

        new Thread(() -> {
            System.out.println("客户端启动成功-------");
            while (true) {
                String message = "hello world " + i++;
                try {
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                    System.out.println("客户端发送：" + message);
                    InputStream inputStream = client.getInputStream();
                    byte[] date = new byte[1024];
                    int len;
                    // inputStream.read 会阻塞等待再次输入
                    while ((len = inputStream.read(date)) != -1) {
                        String msg = new String(date, 0, len);
                        System.out.println("客户端传来消息：" + msg);
                        // \n 标示结束
                        if (msg.endsWith("\n")) {
                            break;
                        }
                    }
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
```



#### netty 对Socket的抽象

![](img\2022-06-25 031111.png)



##### NioEvenLoop 监听端口

主要两个循环处理 

- for (;;) 循环处理 新链接

  简单理解--serverSocket.accept()

- for (;;) f循环处理 数据流

   简单理解--HandlerClient.handler()



##### Channel 连接

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



##### Bytebuf  接收的数据

IO的读写



##### ChannelHandler 业务处理

数据包的处理
















































