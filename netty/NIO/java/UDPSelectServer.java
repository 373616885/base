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
                    ByteBuffer byteBuffer = ByteBuffer.allocate(3);
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
