### Recycler

对象回收池

优点：

减少 new 对于属性较少的是不合适的，想申请内存这样的耗时new 和频繁的才合适

减少的对象同时也减轻的Yang GC的压力





使用：

对象必须有一个Recycler.Handle<User> handle 属性

通过这个handle 去回收对象

参考：netty的PooledDirectByteBuf

```java
import io.netty.util.Recycler;
import lombok.AllArgsConstructor;
import lombok.Data;

public class RecycleUtil {

    private static final Recycler<User> RECYCLER = new Recycler<>() {
        @Override
        protected User newObject(Handle<User> handle) {
            return new User(handle, 32, "qinjp");
        }
    };

    @Data
    @AllArgsConstructor
    static class User {
        private Recycler.Handle<User> handle;
        private int age;
        private String name;

        public void recycle(){
            handle.recycle(this);
        }
    }

    public static void main(String[] args) {
        final User user0 = RECYCLER.get();
        System.out.println(user0);
        user0.recycle();
        final User user1 = RECYCLER.get();
        System.out.println(user1);
        System.out.println(user1 == user0);
    }


}
```





###  原理

每一个Thead都绑定一个LocalPool放到FastThreadLocall里，获取对象的时候，先获取当前线程的LocalPool，然后从stack里获取，有就拿，没就创建

然后从LocalPool里获取DefaultHandle，有返回 DefaultHandle.value  这就是我们之前放的值

没有创建一个 DefaultHandle 里面的 value  就等于之前放的值 ，同时保存上面的 LocalPool

DefaultHandle 的 recycle 方法就是使用 LocalPool就对象放到自己的Queue里面





### 同线程回收

由于数据对象有 Handle 属性，Handle里保存着 LocalPool -- 对象就放到 LocalPool.Queue里面

LocalPool是和线程绑定的



### 新版没有异线程回收的说法











