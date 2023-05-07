package com.qin.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 服务端处理客户端
 */
public class HandlerClient {
    private static final int MAX_DATA_LEN = 1024;

    private final Socket client;

    public HandlerClient(Socket client) {
        this.client = client;
    }

    public void start() {

        new Thread(() -> {
            // 这里要创建一个新线程
            // 因为客户端的读写会阻塞影响到服务端的accept
            handler();
        }).start();
    }

    private void handler() {
        try {
            InputStream inputStream = client.getInputStream();
            while (true) {
                byte[] date = new byte[MAX_DATA_LEN];
                int len;
                // inputStream.read 会阻塞等待再次输入
                while ((len = inputStream.read(date)) != -1) {
                    String message = new String(date, 0, len);
                    System.out.println("客户端传来消息：" + message);
                    // 返回客户端数据  "\n" 标示结束符，客户端自己处理
                    String result = "收到数据：" + message + "\n";
                    // 给客户端返回数据
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(result.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
