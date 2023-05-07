### 单例模式

**一个类全局只有一个对象**

​	私有构造器

**延迟创建**

​	静态内部类，第一次加载Singleton类的时候并不会初始化instance，只有第一次调用getInstance() 才会导致虚拟机加载SingletonHolder类

**线程安全**

​	静态内部类JVM保证线程安全

```java
public final class Singleton {

	/**
	 * 在《Java并发编程实践》推荐使用如下代码：
	 * 静态类内部加载:
	 *    第一次加载Singleton类的时候并不会初始化instance，
	 *    只有第一次调用Singleton的getInstance() 才会导致instance初始化
	 *    因此，第一次调用getInstance() 方法会导致虚拟机加载SingletonHolder类
	 *    这种方式不仅能够保证线程安全，也能保证单例对象的唯一性，同事也延迟了单例的实例化
	 */
	private Singleton(){}
    public static Singleton getInstance(){
         return SingletonHolder.instance;
    }
    
    private static class SingletonHolder{
        private static final Singleton instance = new Singleton();
    }

}
```

枚举单例模式

```java
public enum SingletonEnum {

	/**
	 * Effective Java作者Josh Bloch 提倡的方式，简洁而完美
	 * 涉及到反序列化创建对象时会试着使用枚举的方式来实现单例
	 * Java虚拟机会保证枚举类型不能被反射并且构造函数只被执行一次
	 */
	INSTANCE;
	public void methods() {
		System.out.println("SingletonEnum");
	}
	
	public static void main(String[] args) {
		SingletonEnum.INSTANCE.methods();
	}
}

```



### netty 单例模式

ReadTimeoutException 和  MqttEncoder

```java
//获取ReadTimeoutException.INSTANCE
public final class ReadTimeoutException extends TimeoutException {

    private static final long serialVersionUID = 169287984113283421L;

    public static final ReadTimeoutException INSTANCE = PlatformDependent.javaVersion() >= 7 ?
            new ReadTimeoutException(true) : new ReadTimeoutException();

    public ReadTimeoutException() { }

    public ReadTimeoutException(String message) {
        super(message, false);
    }

    private ReadTimeoutException(boolean shared) {
        super(null, shared);
    }
}

//获取 MqttEncoder.INSTANCE
public final class MqttEncoder extends MessageToMessageEncoder<MqttMessage> {

    public static final MqttEncoder INSTANCE = new MqttEncoder();

    private MqttEncoder() { }

    @Override
    protected void encode(ChannelHandlerContext ctx, MqttMessage msg, List<Object> out) throws Exception {
        out.add(doEncode(ctx, msg));
    }
	....
        
}        
```







### 策略模式

**封装一系列动态的可替换的算法**

**动态选择某个策略**



例如：缓存

一系列算法redis ,local 的

都实现一个cache接口

里面的getCache可以通过算法路由到需要用哪个缓存算法

```java
package com.qin.util;

/**
 * 缓存策略
 */
public final class CacheStrategy {
    //一系动态列算法 start
    private final Cache localCache = new LocalCache();
    private final Cache redisCache = new RedisCache();

    public interface Cache {
        boolean add(String key, Object obj);

        Object get(String key);
    }

    public class LocalCache implements Cache {

        @Override
        public boolean add(String key, Object obj) {
            // 保存到本地
            return false;
        }

        @Override
        public Object get(String key) {
            return null;
        }

    }

    public class RedisCache implements Cache {

        @Override
        public boolean add(String key, Object obj) {
            // 保存到Redis
            return false;
        }

        @Override
        public Object get(String key) {
            return null;
        }

    }
    //一系动态列算法 end

	// 动态选择某个策略 
    public Cache getCache(String key) {
        //动态选择某个策略 
        if (key.length() > 10) {
            return localCache;
        }
        return redisCache;
    }
	
    //对外暴露的
    public Object get(String key) {
        return getCache(key).get(key);
    }
	//对外暴露的
    public void add(String key, Object obj) {
        getCache(key).add(key, obj);
    }

}
```





### netty 策略模式 

 DefaultEventExecutorChooserFactory.newChooser

```java
public final class DefaultEventExecutorChooserFactory implements EventExecutorChooserFactory {

    public static final DefaultEventExecutorChooserFactory INSTANCE = new DefaultEventExecutorChooserFactory();

    private DefaultEventExecutorChooserFactory() { }
	
    // 动态选择某个策略
    public EventExecutorChooser newChooser(EventExecutor[] executors) {
        if (isPowerOfTwo(executors.length)) {
            return new PowerOfTwoEventExecutorChooser(executors);
        } else {
            return new GenericEventExecutorChooser(executors);
        }
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }
	// idx是2指数幂的算法
    private static final class PowerOfTwoEventExecutorChooser implements EventExecutorChooser {
        private final AtomicInteger idx = new AtomicInteger();
        private final EventExecutor[] executors;

        PowerOfTwoEventExecutorChooser(EventExecutor[] executors) {
            this.executors = executors;
        }

        @Override
        public EventExecutor next() {
            return executors[idx.getAndIncrement() & executors.length - 1];
        }
    }
	// idx非2指数幂的算法
    private static final class GenericEventExecutorChooser implements EventExecutorChooser {
        private final AtomicLong idx = new AtomicLong();
        private final EventExecutor[] executors;

        GenericEventExecutorChooser(EventExecutor[] executors) {
            this.executors = executors;
        }

        @Override
        public EventExecutor next() {
            return executors[(int) Math.abs(idx.getAndIncrement() % executors.length)];
        }
    }
}
```







### 装饰者模式

**装饰者和被装饰者继承同一个接口**

**装饰者给被装饰者一个动态修改行为**



装饰者在使用接口的时候，获取被装饰者的接口

在装饰者内部动态调用被装饰者的接口，或者修改其行为（原始的被Override）



```java

import java.sql.SQLOutput;

/**
 * 装饰者
 */
public class Decorator {

    //销售价格
    public interface Sale {
        float getPrice(float oldMoney);
    }

    //立减优惠
    public static class KnockMoney implements Sale {
        //优惠金额
        private float amount = 50;

        @Override
        public float getPrice(float oldMoney) {
            if (oldMoney > 50) {
                return oldMoney - 50;
            }
            return oldMoney;
        }
    }

    //打折优惠
    public static class DisMoney implements Sale {
        //打折折扣金额 2折
        private float disMoney = 2;
        //被装饰者
        //在装饰者内部动态调用
        private Sale sale;

        public DisMoney() {
            // 默认啥事不干直接返回
            sale = oldMoney -> oldMoney;
        }

        public DisMoney(Decorator.Sale sale) {
            this.sale = sale;
        }

        @Override
        public float getPrice(float oldMoney) {
            // 内部动态调用被装饰者的接口
            return sale.getPrice(oldMoney) * disMoney / 10;
        }
    }

    public static void main(String[] args) {
        Sale knockMoney = new KnockMoney();
        Sale disMoney  = new DisMoney(knockMoney);

        final float price = disMoney.getPrice(100);

        System.out.println(price);
    }


}



```







### netty 装饰者模式

io.netty.buffer.WrappedByteBuf 

io.netty.buffer.UnreleasableByteBuf  

io.netty.buffer.SimpleLeakAwareByteBuf

```java
class WrappedByteBuf extends ByteBuf {
	//被装饰者
    protected final ByteBuf buf;

    protected WrappedByteBuf(ByteBuf buf) {
        this.buf = ObjectUtil.checkNotNull(buf, "buf");
    }
	//下面这些方法都委托被装饰者ByteBuf去调用
    @Override
    public final boolean hasMemoryAddress() {
        return buf.hasMemoryAddress();
    }
    .....
}
	
final class UnreleasableByteBuf extends WrappedByteBuf {
    
	//WrappedByteBuf才是被装饰者
    UnreleasableByteBuf(ByteBuf buf) {
        super(buf instanceof UnreleasableByteBuf ? buf.unwrap() : buf);
    }
	
    //调用被装饰者
    @Override
    public ByteBuf asReadOnly() {
        ////调用被装饰者动态修改行为--额外行为
        return buf.isReadOnly() ? this : new UnreleasableByteBuf(buf.asReadOnly());
    }
    
    //特殊的覆盖
    //动态修改的目的
    @Override
    public boolean release() {
        return false;
    }
    ......
}


```







### 观察者模式

观察者和被观察者

观察者订阅消息被观察者发布消息

订阅才能收到消息，取消就无法手动消息



java 有对应的接口 ： Observable 和  Observer



```java

import java.util.ArrayList;
import java.util.List;

public class ObservableUtil {

    // 被观察者
    public interface Observable {
        //容器
        final List<Observer> OBSERVER_LIST = new ArrayList<>();

        //注册到容器观察者里
        void register(Observer observer);

        //移除容器里的观察者里
        void remove(Observer observer);

        //通知所有的观察者
        void notifyObserver(String msg);
    }


    // 观察者
    public interface Observer {
        void receive(String msg);
    }

    public static class Client implements Observer {

        private String name;

        public Client(String name) {
            this.name = name;
        }

        @Override
        public void receive(String msg) {
            System.out.println("客户的端: " + name + " 收到消息: " + msg);
        }
    }

    public static class Server implements Observable {

        @Override
        public void register(Observer observer) {
            OBSERVER_LIST.add(observer);
        }

        @Override
        public void remove(Observer observer) {
            //内部循环判断去删除
            OBSERVER_LIST.remove(observer);
        }

        @Override
        public void notifyObserver(String msg) {
            for (Observer observer : OBSERVER_LIST) {
                observer.receive(msg);
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server();

        Client client01 = new Client("01");
        Client client02 = new Client("02");

        server.register(client01);
        server.register(client02);


        server.notifyObserver("服务端快奔溃了");

    }

}
```





### netty 观察者

ChannelFuture 其实就一个被观察者

```java
ChannelFuture channelFuture = ctx.pipeline().writeAndFlush(user);
// 添加观察者
channelFuture.addListener(future -> {
    if(future.isSuccess) {
        
    } else {
        
    }
});
// 添加观察者
channelFuture.addListener(future -> {
    if(future.isSuccess) {
        
    } else {
        
    }
});
```





### 责任链模式

责任处理器接口

创建链，添加删除责任处理器接口

上下文

责任终止机制

![](img\2022-09-12 060736.png)



### netty 责任链模式

**责任处理器接口**

ChannelHandler   ChannelInboundHandler ChannelOutboundHandler



**创建链，添加删除责任处理器接口 ** 

ChannelPipeline  接口的方法

addFirst  addLast  addLast  remove removeFirst  removeLast

默认实现：

DefaultChannelPipeline



**上下文 **

ChannelHandlerContext

可以拿到很多信息  next   prev  pipeline   channel  executor 



**责任终止机制**

一般是人通过true false 来判断是否继续向下传播的

netty 通过 ctx.fireXXX 继续向下传播的



















 



























