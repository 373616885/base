package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandler implements Runnable {
    private int port;
    private String host;
    private Selector selector;
    private SocketChannel channel;
    private volatile boolean stop;

    public TimeClientHandler(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        while (!stop) {
            try {
                // selector每一秒被唤醒一次
                selector.select(1000);
                // 还回就绪状态的chanel的 selectedKeys
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            selector = null;
        }
    }

    public void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else {
                    System.exit(1);// 连接失败，进程退出
                }
            }
            if (key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("当前时间 : " + body);
                    this.stop = true;
                } else if (readBytes < 0) { // 对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    ; // 读到0字节，忽略
                }
            }
        }
    }

    private void doConnect() throws IOException {
        if (channel.connect(new InetSocketAddress(host, port))) {
            channel.register(selector, SelectionKey.OP_READ);
            doWrite(channel);
        } else {
            channel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel schannel) throws IOException {
        byte[] bytes = "QUERY TIME".getBytes();
        ByteBuffer buff = ByteBuffer.allocate(bytes.length);
        buff.put(bytes);
        buff.flip();
        schannel.write(buff); // 判断是否发送完毕
        if (!buff.hasRemaining()) {
            System.out.println("发送成功!");
        }
    }
}
