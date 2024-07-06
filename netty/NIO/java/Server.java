import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("this server start------");
        while (true) {
            // 这里会阻塞-等待新的客户端进来
            Socket client = serverSocket.accept();
            System.out.println("new socket accept------" + client.getRemoteSocketAddress());
            handler(client);
        }
    }

    private static void handler(Socket client) throws IOException {
        try {
            InputStream inputStream = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();
            byte[] date = new byte[1024];
            int len = inputStream.read(date);
            String message = new String(date, 0, len);
            System.out.println("received length: " + len);
            System.out.println("received message: " + message);
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.write("HTTP/1.1 200 OK\r\n");
            // 请求头与请求体要用空行分割
            printWriter.write("Content-Type: text/html; charset=utf-8\n\n");
            printWriter.write("<h1>received length: " + len + "</h1>\n");
            printWriter.write("<h1>received msg</h1>\n");
            printWriter.write("<p style='font-size:16px;white-space: pre-line;'>" + message + "</p>");
            printWriter.write("\n");
            printWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
