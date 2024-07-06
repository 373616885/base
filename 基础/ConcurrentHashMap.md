### ConcurrentHashMap（肯课为HashMap）如何保证线程安全

一、Unsafe和CAS

ConcurrentHashMap的大部分操作和HashMap是相同的，例如初始化，扩容和链表向红黑树的转变等。但是，在ConcurrentHashMap中，大量使用了U.compareAndSwapXXX

的方法，这个方法是利用一个CAS算法实现无锁化的修改值的操作，他可以大大降低锁代理的性能消耗。这个算法的基本思想就是不断地去比较当前内存中的变量值与你指定的

一个变量值是否相等，如果相等，则接受你指定的修改的值，否则拒绝你的操作。因为当前线程中的值已经不是最新的值，你的修改很可能会覆盖掉其他线程修改的结果。这一

点与乐观锁，SVN的思想是比较类似的。

```java
 // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long SIZECTL;
    private static final long TRANSFERINDEX;
    private static final long BASECOUNT;
    private static final long CELLSBUSY;
    private static final long CELLVALUE;
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentHashMap.class;
            SIZECTL = U.objectFieldOffset
                (k.getDeclaredField("sizeCtl"));
            TRANSFERINDEX = U.objectFieldOffset
                (k.getDeclaredField("transferIndex"));
            BASECOUNT = U.objectFieldOffset
                (k.getDeclaredField("baseCount"));
            CELLSBUSY = U.objectFieldOffset
                (k.getDeclaredField("cellsBusy"));
            Class<?> ck = CounterCell.class;
            CELLVALUE = U.objectFieldOffset
                (ck.getDeclaredField("value"));
            Class<?> ak = Node[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
```

同时，在ConcurrentHashMap中还定义了三个原子操作，用于对指定位置的节点进行操作。这三种原子操作被广泛的使用在ConcurrentHashMap的get和put等方法中，

正是这些原子操作保证了ConcurrentHashMap的线程安全。

```java
// 获取tab数组的第i个node
static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
    return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
}
// 利用CAS算法设置i位置上的node节点。在CAS中，会比较内存中的值与你指定的这个值是否相等，如果相等才接受
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                    Node<K,V> c, Node<K,V> v) {
    return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
}
// 利用volatile方法设置第i个节点的值，这个操作一定是成功的。
static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
    U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
}
```

**二、ConcurrentHashMap的put方法**

接下来，我们来看下ConcurrentHashMap中最主要的put方法的实现，在put方法中调用了putVal方法，其源码如下：

```java
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    // 计算hash值
    int hash = spread(key.hashCode());
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        if (tab == null || (n = tab.length) == 0)
            tab = initTable(); // table是在首次插入元素的时候初始化，lazy
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null, // 如果这个位置没有值，直接放进去，由CAS保证线程安全，不需要加锁
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        else if ((fh = f.hash) == MOVED)//如果找到的桶存在，但是桶中第一个元素的hash值是-1，说明此时该桶正在进行迁移操作
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            synchronized (f) {  // 节点上锁，这里的节点可以理解为hash值相同组成的链表的头节点，锁的粒度为头节点。
                if (tabAt(tab, i) == f) {
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            // 存在就覆盖老值
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            // 之前不存在就在链表末尾添加
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);
    return null;
}
```

- 如果数组还未初始化，那么进行初始化，这里会通过一个CAS操作将sizeCtl设置为-1，设置成功的，可以进行初始化操作
- 根据key的hash值找到对应的桶，如果桶还不存在，那么通过一个CAS操作来设置桶的第一个元素，失败的继续执行下面的逻辑即向桶中插入或更新
- 如果找到的桶存在，但是桶中第一个元素的hash值是-1，说明此时该桶正在进行迁移操作，这一块会在下面的扩容中详细谈及。
- 如果找到的桶存在，那么要么是链表结构要么是红黑树结构，此时需要获取该桶的锁，在锁定的情况下执行链表或者红黑树的插入或更新
  - 如果桶中第一个元素的hash值大于0，说明是链表结构，则对链表插入或者更新 --在链表末尾添加
  - 如果桶中的第一个元素类型是TreeBin，说明是红黑树结构，则按照红黑树的方式进行插入或者更新
- 在锁的保护下插入或者更新完毕后，如果是链表结构，需要判断链表中元素的数量是否超过8（默认），一旦超过就要考虑进行数组扩容或者是链表转红黑树



因此，我们可以发现JDK8中ConcurrentHashMap的实现使用的是锁分离思想，只是锁住的是一个node，而锁住Node之前的操作是基于在volatile和CAS之上无锁并且线程安全的。







