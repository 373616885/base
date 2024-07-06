package com.qin.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;

    public Server(Integer port) {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("服务器启动成功------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(() -> {
            doStart();
        }).start();
    }

    public void doStart(){
        while (true) {
            try {
                // 这里会阻塞-等待新的客户端进来
                Socket client = serverSocket.accept();
                System.out.println("新的连接------" + client.getRemoteSocketAddress()) ;
                new HandlerClient(client).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
