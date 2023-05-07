import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class FileDemo1 {

    public static void main(String[] args) throws IOException {
        File file = new File("text1.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
        FileChannel fc = randomAccessFile.getChannel();
        fc.map(FileChannel.MapMode.READ_WRITE,0,10);
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("qinjiepeng".getBytes(StandardCharsets.UTF_8));
        System.out.println(buffer.toString());
        buffer.flip();
        fc.position(0).write(buffer);
        fc.force(true);
        fc.close();
        randomAccessFile.close();

    }

}
