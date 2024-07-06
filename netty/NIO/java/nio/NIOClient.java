package nio;

public class NIOClient {
    public static void main(String[] args) {
        int port = 9999;
        new Thread(new TimeClientHandler("127.0.0.1", port)).start();
    }
}
