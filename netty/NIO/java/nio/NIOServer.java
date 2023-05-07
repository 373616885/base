package nio;

public class NIOServer {

    public static void main(String[] args) {
        int port = 9999;
        MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
        new Thread(timeServer, "NIO-TimeServer").start();
    }

}
