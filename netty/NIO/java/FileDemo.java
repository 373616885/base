import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDemo {

    public static void main(String[] args) throws IOException {
        File file = new File("/usr/local/java/text.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        for (int i = 0; i < 10; i++) {
            out.write('a');
            out.flush();
        }
        out.close();
    }
}
