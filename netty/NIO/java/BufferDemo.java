import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BufferDemo {

    public static void buffer( ) {
        // 创建buffer
        IntBuffer intBuffer = IntBuffer.allocate(5);
        // 写操作
        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i * 5);
        }
        // 由写转读操作需要flip操作
        // limit = position;
        // position = 0;
        // mark = -1;
        intBuffer.flip();

        while (intBuffer.hasRemaining()) {
            //顺序读
            int tmp = intBuffer.get();
            System.out.println("顺序读结果：" + tmp);
        }
        // 读取指定位置
        IntBuffer two = intBuffer.position(2);
        System.out.println("指定读位置：" + two.get());
        System.out.println("指定读位置：" + intBuffer.get(2));

    }


    public static void directBuffer(){
        // 创建buffer
        ByteBuffer buffer = ByteBuffer.allocateDirect(5);
        // 写操作
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put((byte) i);
        }
        // 由写转读操作需要flip操作
        // limit = position;
        // position = 0;
        // mark = -1;
        buffer.flip();

        while (buffer.hasRemaining()) {
            //顺序读
            int tmp = buffer.get();
            System.out.println("顺序读结果：" + tmp);
        }
        // 读取指定位置
        ByteBuffer two = buffer.position(2);
        System.out.println("指定读位置：" + two.get());
        System.out.println("指定读位置：" + buffer.get(2));
    }

    public static void chean(ByteBuffer buffer) {
        if(buffer.isDirect()) {
            //((DirectBuffer)buffer).cleaner().clean();
        } else {
            buffer.clear();
        }

    }



    public static void main(String[] args) {
        directBuffer();
    }


}



