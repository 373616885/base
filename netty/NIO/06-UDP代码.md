

### UDP代码

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



























































