import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteBufferDemo {

    public static void mappedByteBuffer() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("a.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();
        /**
         * 参数1：FileChannel.MapMode.READ_WRITE 使用读写模式
         * 参数2：内存起始位置
         * 参数3：映射内存的大小,即将a.txt的多少个字节映射到内存中
         * 超过大小，会报错 IndexOutOfBoundsException
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0, "Q".getBytes());
        mappedByteBuffer.put(1, "J".getBytes());
        mappedByteBuffer.put(2, "P".getBytes());
        // 报错 IndexOutOfBoundsException
        // mappedByteBuffer.put(5, "P".getBytes());
        randomAccessFile.close();
        System.out.println("修改结束");

    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

}
