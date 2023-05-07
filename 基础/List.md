### List

 Java的List是非常常用的数据类型。List是有序的Collection。Java List一共三个实现类： 分别是ArrayList、Vector和LinkedList。 

![](img\20200515020114.png)



### ArrayList（数组） 

ArrayList 是最常用的 List 实现类，内部是通过数组实现的，它允许对元素进行快速随机访问。数 组的缺点是每个元素之间不能有间隔，当数组大小不满足时需要增加存储能力，就要将已经有数 组的数据复制到新的存储空间中。当从 ArrayList 的中间位置插入或者删除元素时，需要对数组进 行复制、移动、代价比较高。因此，它适合随机查找和遍历，不适合插入和删除。 

###  Vector（数组实现、线程同步） 

Vector与ArrayList一样，也是通过数组实现的，不同的是它支持线程的同步，即某一时刻只有一 个线程能够写 Vector，避免多线程同时写而引起的不一致性，但实现同步需要很高的花费，因此， 访问它比访问ArrayList慢。 

### LinkList（链表） 

LinkedList是用链表结构存储数据的，很适合数据的动态插入和删除，随机访问和遍历速度比较 慢。另外，他还提供了List接口中没有定义的方法，专门用于操作表头和表尾元素，可以当作堆 栈、队列和双向队列使用



### ArrayList  源代码

```java
// 默认大小是 10
private static final int DEFAULT_CAPACITY = 10;

transient Object[] elementData; 

private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
// 默认空数组
public ArrayList() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}
// 初始化一个固定容量
public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
        throw new IllegalArgumentException("Illegal Capacity: "+
                                           initialCapacity);
    }
}
// 添加一个元素
public boolean add(E e) {
    // size 原大小加一
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    // 数组大小加一等于添加的元素
    elementData[size++] = e;
    return true;
}

private void ensureCapacityInternal(int minCapacity) {
    ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}

private void ensureExplicitCapacity(int minCapacity) {
    modCount++;

    // size + 1 大于 elementData 数组长度就进行扩容
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}
// 扩容规则-- newCapacity = oldCapacity + (oldCapacity >> 1)
// 原来大小 + 原大小/2 --- 就是原大小的1.5倍 
private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);
}


```



### ArrayList   为什么不是线程安全的

 add元素时，实际做了两个大的步骤： 

1. 判断elementData数组容量是否满足需求
2. 在elementData对应位置上设置值

这样也就出现了第一个导致线程不安全的隐患，在多个线程进行add操作时可能会导致elementData数组越界。具体逻辑如下：

1. 列表大小为9，即size=9
2. 线程A开始进入add方法，这时它获取到size的值为9，调用ensureCapacityInternal方法进行容量判断。
3. 线程B此时也进入add方法，它获取到size的值也为9，也开始调用ensureCapacityInternal方法。
4. 线程A发现需求大小为10，而elementData的大小就为10，可以容纳。于是它不再扩容，返回。
5. 线程B也发现需求大小为10，也可以容纳，返回。
6. 线程A开始进行设置值操作， elementData[size++] = e 操作。此时size变为10。
7. 线程B也开始进行设置值操作，它尝试设置elementData[10] = e，而elementData没有进行过扩容，它的下标最大为9。于是此时会报出一个数组越界的异常ArrayIndexOutOfBoundsException.

另外第二步 elementData[size++] = e 设置值的操作同样会导致线程不安全。从这儿可以看出，这步操作也不是一个原子操作，它由如下两步操作构成：

1. elementData[size] = e;
2. size = size + 1;

在单线程执行这两条代码时没有任何问题，但是当多线程环境下执行时，可能就会发生一个线程的值覆盖另一个线程添加的值，具体逻辑如下：

1. 列表大小为0，即size=0
2. 线程A开始添加一个元素，值为A。此时它执行第一条操作，将A放在了elementData下标为0的位置上。
3. 接着线程B刚好也要开始添加一个值为B的元素，且走到了第一步操作。此时线程B获取到size的值依然为0，于是它将B也放在了elementData下标为0的位置上。
4. 线程A开始将size的值增加为1
5. 线程B开始将size的值增加为2

这样线程AB执行完毕后，理想中情况为size为2，elementData下标0的位置为A，下标1的位置为B。而实际情况变成了size为2，elementData下标为0的位置变成了B，下标1的位置上什么都没有。并且后续除非使用set方法修改此位置的值，否则将一直为null，因为size为2，添加元素时会从下标为2的位置上开始。 

### 总结有两个问题：

1. 线程A 和 线程B 在 add 扩容临界的时候 ， 线程A 不需要扩容 ，同时线程B 也不需要扩容 ，但操作elementData[size++] = e 时 ， 线程 A 已经是数组最大的临界的值了，线程B 再操作 elementData[size++] size++ 已经超过数组的最大值了--  造成数组越界的异常ArrayIndexOutOfBoundsException
2. elementData[size++] = e  这个操作分为两步 elementData[size] = e; 和  size = size + 1; -- elementData[size] = e 这一步 两个线程同时操作 造成 值覆盖的情况 ，而接下来 都进行 size + 1 就 造成有一个值永远为 null 

### 解决方法：

**1.把ArrayList<>();改成Vector<>();**

**2.把new ArrayList<>();改成Collections.synchronizedList(new ArrayList<>());**



### Vector 

其实和ArrayList 差不多只是 在ArrayList 操作法上加上 synchronized 避免多线程同时写而引起的不一致性

```java
// 元素
protected Object[] elementData;
// 个数 和 ArrayList 的 size 一样
protected int elementCount;
// 初始化大小默认10
public Vector() {
    this(10);
}
// initialCapacity 初始容量
public Vector(int initialCapacity) {
    this(initialCapacity, 0);
}
// initialCapacity 初始容量 和 capacityIncrement 增长个数
public Vector(int initialCapacity, int capacityIncrement) {
    super();
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal Capacity: "+
                                           initialCapacity);
    this.elementData = new Object[initialCapacity];
    this.capacityIncrement = capacityIncrement;
}
// 添加操作上 使用 synchronized 避免多线程同时写而引起的不一致性 
public synchronized void addElement(E obj) {
    modCount++;
    ensureCapacityHelper(elementCount + 1);
    elementData[elementCount++] = obj;
}
// 递增规则 如果 capacityIncrement > 0 就按照这个参数递增
// 否则就 oldCapacity + oldCapacity 原来的两倍
private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                     capacityIncrement : oldCapacity);
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    elementData = Arrays.copyOf(elementData, newCapacity);
}
   
```



### LinkedList

```java
transient int size = 0;
// 链表的头
transient Node<E> first;
// 链表的尾
transient Node<E> last;
// 数据
private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;

    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
// 默认添加元素是在末尾添加的
public boolean add(E e) {
    linkLast(e);
    return true;
}
// 
void linkLast(E e) {
    // 原末尾数据
    final Node<E> l = last;
    // 新的末尾数据 prv=原末尾数据 ， e , next = null 
    final Node<E> newNode = new Node<>(l, e, null);
    // 末尾数据等于新生成的数据
    last = newNode;
    // 原末尾数据==null 证明 是第一次添加
    if (l == null)
        first = newNode;
    else
        // 原数据的末尾等于 新添加的数据
        l.next = newNode;
    size++;
    modCount++;
}
// 获取数据
public E get(int index) {
    checkElementIndex(index);
    return node(index).item;
}
// 二分法 小于一半就 从头开始递归
//       大于一半就 从尾开始递归
Node<E> node(int index) {
    // assert isElementIndex(index);
    if (index < (size >> 1)) {
         // 小于一半就 从头开始递归
        Node<E> x = first;
        for (int i = 0; i < index; i++)
            x = x.next;
        return x;
    } else {
        //大于一半就 从尾开始递归
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
            x = x.prev;
        return x;
    }
}

```

**LinkedList是用链表结构存储数据的**

操作数据的时候 直接 对元素node 的 prev和next 操作 不需要像ArrayList 那样在扩容的时候需要进行数组的复制

所以数据的动态插入和删除比较快

但是获取数据的时候 通过  二分法for循环查找 （小于一半就 从头开始递归  大于一半就 从尾开始递归 ）

所以随机访问和遍历速度比较 慢

