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
