import jdk.internal.ref.Cleaner;

public class CleanDemo {
    private final Cleaner cleaner;

    public CleanDemo() {
        cleaner = Cleaner.create(this, () -> {
            System.out.println("GC --clean");
        });
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            CleanDemo ob = new CleanDemo();
            Thread.sleep(1000);
            System.out.println("ob==");
            if(i % 2 == 0){
                System.gc();
            }
        }
    }

}
