### Socket 通信代码

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
        // 这里new一个线程，绑定端口的主线程，不要影响监听的线程
        // 端口监听放到一个单独的线程里去做
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
                // 每一个客户端都需要一个新线程
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
            // 每一个客户端都需要一个新线程处理
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
                    // inputStream.read 会阻塞等待服务端再次输入
                    while ((len = inputStream.read(date)) != -1) {
                        String msg = new String(date, 0, len);
                        System.out.println("客户端传来消息：" + msg);
                        // \n 标示结束
                        if (msg.endsWith("\n")) {
                            break;
                        }
                    }
                    // 同一个连接休息5s之后继续发送
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
```



### Socket 通信过程

1. 服务端监听端口
2. 服务端accept新连接
3. 服务端处理接收数据
4. 业务逻辑处理
5. 发送数据给客户端



![](img\2022-08-22 024346.png)





### netty 对Socket通信过程的抽象

![](img\2022-06-25 031111.png)





1. 服务端监听端口对应NioEvenLoop
2. 新连接对应Channel 
3. 处理接收数据对应ByteBuf
4. 业务逻辑对应ChannelHandler 
5. 发送数据对应ByteBuf


















































