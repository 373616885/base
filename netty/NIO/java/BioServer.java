import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BioServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务启动完成");
        while (true) {
            // 这里会阻塞-等待新的客户端进来
            Socket socket = serverSocket.accept();
            System.out.println("新的连接------" + socket.getRemoteSocketAddress());

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            String result = "收到数据：";
            byte[] date = new byte[1024];
            int len;
            // inputStream.read 会阻塞等待再次输入
            while ((len = inputStream.read(date)) != -1) {
                String message = new String(date, 0, len);
                result = result + message;
                System.out.println(result);
            }
            // 返回客户端数据  "\n" 标示结束符，客户端自己处理
            result = result + "\n";
            System.out.println("客户端传来消息：" + result);
            // 给客户端返回数据
            outputStream.write(result.getBytes(StandardCharsets.UTF_8));

            outputStream.flush();
            System.out.println("传递客户端消息结束");

            inputStream.close();
            outputStream.close();
            socket.close();
            // 最后通过nc 的方式连接 发生数据
            // 断开 inputStream.read 就不会阻塞等待，程序就可以往下走

        }

    }
}
