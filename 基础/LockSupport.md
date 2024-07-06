  Object 的wait和notify/notifyAll 方法只能在 **同步代码块** 里用(这个有的面试官也会考察) 

```java

public class TestObjWait1 {

    public static void main(String[] args)throws Exception {
        final Object obj = new Object();
        
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for(int i=0;i<10;i++){
                    sum+=i;
                }
                try {
                    synchronized (obj){
                        obj.wait();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                
                System.out.println(Thread.currentThread().getName()+"  sum = "+sum);
            }
        },"ThreadA");
        
        threadA.start();
        
        //睡眠一秒钟，保证线程A已经计算完成，阻塞在wait方法
        Thread.sleep(1000);
        System.out.println(Thread.currentThread().getName()+" sleep over ");
        
        synchronized (obj){
            obj.notify();
        }
        System.out.println(Thread.currentThread().getName()+" over ");
    }
}

运行结果：
main sleep over 
main over 
ThreadA  sum = 45
```

**缺点：**  

1. 只能在 **同步代码块** 里用 -- 需要维护一个共享的同步对象
2.  obj.notify();  必须保证在  obj.wait();  之后 不然会出现  线程无法唤醒的情况

上面的代码如果去掉  Thread.sleep(1000);

就会造成 obj.wait(); 一直等待的情况  从而无法输出 ThreadA  sum = 45



避免这种情况可以使用  LockSupport  

```java
import java.util.concurrent.locks.LockSupport;

public class TestObjWait2 {

    public static void main(String[] args)throws Exception {
    	
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for(int i=0;i<10;i++){
                    sum+=i;
                }
                LockSupport.park();
                System.out.println(Thread.currentThread().getName()+ " sum = "+sum);
            }
        },"ThreadA");
        
        threadA.start();
        
        //睡眠一秒钟，保证线程A已经计算完成，阻塞在wait方法
        //Thread.sleep(1000);
        System.out.println(Thread.currentThread().getName()+ " sleep over ");
        
        LockSupport.unpark(threadA);
        System.out.println(Thread.currentThread().getName()+ " over ");
    }
}
```



**总结一下，LockSupport比Object的wait/notify有两大优势**：

①LockSupport不需要在同步代码块里 。所以线程间也不需要维护一个共享的同步对象了，实现了线程间的解耦。

②unpark函数可以先于park调用，所以不需要担心线程间的执行的先后顺序

