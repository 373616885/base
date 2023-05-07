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
