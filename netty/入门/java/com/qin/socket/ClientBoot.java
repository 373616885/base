package com.qin.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientBoot {

    private static final String HOST = "127.0.0.1";

    private static final Integer PORT = 8888;

    private static final int SLEEP_TIME = 5000;

    private static int i = 1;

    public static void main(String[] args) throws IOException {

        Socket client = new Socket(HOST, PORT);

        new Thread(() -> {
            System.out.println("客户端启动成功-------");
            while (true) {
                String message = "hello world " + i++;
                try {
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                    System.out.println("客户端发送：" + message);
                    InputStream inputStream = client.getInputStream();
                    byte[] date = new byte[1024];
                    int len;
                    // inputStream.read 会阻塞等待再次输入
                    while ((len = inputStream.read(date)) != -1) {
                        String msg = new String(date, 0, len);
                        System.out.println("客户端传来消息：" + msg);
                        if (msg.endsWith("\n")) {
                            break;
                        }
                    }
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
