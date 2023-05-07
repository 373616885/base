Thread.join 等待子线程结束

主线程调用了Thread.join（）后面的代码要等到子线程结束了才能执行

```java
public class JoinThreadTest {
 
    public volatile static int i = 0;
    public static class AddThread extends Thread{
 
        @Override
        public void run() {
            for (i = 0; i < 100000; i++);
        }
    }
 
    public static void main(String[] args) {
        AddThread addThread = new AddThread();
        addThread.start();
        try {
            //主线程等待子子线程完成才能执行后面的
            addThread.join();
            System.out.println(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```



Thread.join(final long millis) 主线程等待子线程死亡的时间最多为 millis 毫秒

```java
public final synchronized void join(final long millis) throws InterruptedException {
    if (millis > 0) {
        if (isAlive()) {
            //用System.nanoTime()比System.currentTimeMillis()
            //测量得到的时间更精确
            final long startTime = System.nanoTime();
            long delay = millis;
            do {
                wait(delay);
            } while (isAlive() && (delay = millis -
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)) > 0);
        }
    } else if (millis == 0) {
        while (isAlive()) {
            wait(0);
        }
    } else {
        throw new IllegalArgumentException("timeout value is negative");
    }
}
```



