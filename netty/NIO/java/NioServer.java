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
        SelectionKey register = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, "qinjp");

        System.out.println("服务器启动成功" + register.toString());

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
                    Object attachment = key.attachment();
                    System.out.println(attachment);
                    // 接受请求
                    ServerSocketChannel sktChannel = (ServerSocketChannel) key.channel();
                    SocketChannel newSocketChannel = sktChannel.accept();
                    // 这里会接收一个客户端SocketChannel的连接请求，并返回对应的SocketChannel
                    // 注意这里如果没有对应的客户端Channel就会返回null
//                    SocketChannel newSocketChannel = serverSocketChannel.accept();
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
