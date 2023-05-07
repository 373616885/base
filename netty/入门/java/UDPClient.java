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

        String msg = "hello UDP server ,I am 覃杰鹏 ," + System.currentTimeMillis();

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
