### HashMap（数组+链表+红黑树）

数据结构 ：数组+链表+红黑树

![](img\20200519004833.png)

```java
// 数组
transient Node<K,V>[] table;
// 链表--next 属性指向下一个
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;

    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
}
```



### HashMap 有几个关键的属性

```java
// 默认大小 16 -- Node<K,V>[] table = new Node[16];
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
// HashMap 大小
transient int size;
// 修改次数
transient int modCount;
// 阈值-- capacity  * loadFactor ( 容量 * 0.75) 当size超过这个时就会进行扩容
int threshold;
// 负载因子 -- 默认等于 DEFAULT_LOAD_FACTOR = 0.75
final float loadFactor; 

// 链表的个数超过 8 就转化成 红黑树
static final int TREEIFY_THRESHOLD = 8;

// 小于 6 就转回链表
static final int UNTREEIFY_THRESHOLD = 6;

// 当数组长度还未达到64个时，优先数组的扩容，否则选择链表转为红黑树。
static final int MIN_TREEIFY_CAPACITY = 64;
```



### HashMap  的容量永远是2的指数幂

因为计算数组下标的时候（hashcode % n ）hashcode 和 数组大小取余 的时候能平均分布

如果不是2的指数幂 会造成有几个位置永远取不到



1. 计算 hash%n  取模 运算的消耗还是比较大的  当n为2的指数次幂时，会满足一个公式：(n - 1) & hash = hash % n，这样就可以用(n - 1) & hash的位运算来使计算更加高效 
2. 如果初始容量是奇数，那么（n-1)就为偶数，偶数2进制的结尾都是0，经过hash值&运算后末尾都是0，那么0001，0011，0101，1001，1011，0111，1101这几个位置永远都不能存放元素了，空间浪费相当大，更糟的是这种情况中，数组可以使用的位置比数组长度小了很多，这样就会造成空间的浪费而且会增加hash冲突。 
3.  要扩容时方便计算，2的幂次方*2 扩容， 容量只是位移一位就可以 。重新计算位置的时候，只需要判断位移的高位hash 就可以知道 节点的位置是 **原位置** 还是 **原位置 +  oldCap**       (e.hash & oldCap) == 0 则是原位置否则就是 **原位置 + oldCap**



```java

// 这里是在指定HashMap 大小的时候 
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                           initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                           loadFactor);
    this.loadFactor = loadFactor;
    // 这里永远返回 2的 指数幂
    this.threshold = tableSizeFor(initialCapacity);
}
/**
 * Returns a power of two size for the given target capacity.
 */
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 扩容规则 新的容量的 = 旧的容量 乘以 2 
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            // 阈值也是  乘以 2 
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) 
        // initial capacity was placed in threshold
        // 初始化的时候 threshold 放的就是容量的大小
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        // 默认16的大小
        newCap = DEFAULT_INITIAL_CAPACITY;
        // 默认阈值 16 * 0.75 = 12
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}


final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // 链表的大小超过 8 的时候就转换成 红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}

```



```java
/**
 * Returns a power of two size for the given target capacity.
 */
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

下面分析这个算法：
首先，为什么要对cap做减1操作。int n = cap - 1;

这是为了防止，cap已经是2的幂。如果cap已经是2的幂， 又没有执行这个减1操作，则执行完后面的几条无符号右移操作之后，返回的capacity将是这个cap的2倍。如果不懂，要看完后面的几个无符号右移之后再回来看看。
下面看看这几个无符号右移操作：
如果n这时为0了（经过了cap-1之后），则经过后面的几次无符号右移依然是0，最后返回的capacity是1（最后有个n+1的操作）。
这里只讨论n不等于0的情况。
第一次右移

```java
n |= n >>> 1;
```

由于n不等于0，则n的二进制表示中总会有一bit为1，这时考虑最高位的1。通过无符号右移1位，则将最高位的1右移了1位，再做或操作，使得n的二进制表示中与最高位的1紧邻的右边一位也为1，如000011xxxxxx。
第二次右移

```java
n |= n >>> 2;
```

注意，这个n已经经过了n |= n >>> 1; 操作。假设此时n为000011xxxxxx ，则n无符号右移两位，会将最高位两个连续的1右移两位，然后再与原来的n做或操作，这样n的二进制表示的高位中会有4个连续的1。如00001111xxxxxx 。
第三次右移

```java
n |= n >>> 4;
```

这次把已经有的高位中的连续的4个1，右移4位，再做或操作，这样n的二进制表示的高位中会有8个连续的1。如00001111 1111xxxxxx 。
以此类推
注意，容量最大也就是32bit的正数，因此最后n |= n >>> 16; ，最多也就32个1（但是这已经是负数了。在执行tableSizeFor之前，对initialCapacity做了判断，如果大于MAXIMUM_CAPACITY(2 ^ 30)，则取MAXIMUM_CAPACITY。如果等于MAXIMUM_CAPACITY(2 ^ 30)，会执行移位操作。所以这里面的移位操作之后，最大30个1，不会大于等于MAXIMUM_CAPACITY。30个1，加1之后得2 ^ 30） 。
举一个例子说明下吧。

![](img\2020023103.png)


这个算法着实牛逼啊！

注意，得到的这个capacity却被赋值给了threshold。

```java
this.threshold = tableSizeFor(initialCapacity);
```

开始以为这个是个Bug，感觉应该这么写：

```java
this.threshold = tableSizeFor(initialCapacity) * this.loadFactor;
```

这样才符合threshold的意思（当HashMap的size到达threshold这个阈值时会扩容）。
但是，请注意，在构造方法中，并没有对table这个成员变量进行初始化，table的初始化被推迟到了put方法中，在put方法中会对threshold重新计算。



### 为什么线程不安全

（1）HashMap 的 size 属性--非典型状况size()的值不准确

```java
/**
 * The number of key-value mappings contained in this map.
 */
transient int size;


```

这个属性并没有 加上  **volatile **关键字修饰 

了解过多线程应该都知道，我们线程操作数据的时候一般是从主存拷贝一个变量副本进行操作。 

![](img\20200519004834.png)



 线程中的变量，都是从主存拷贝过去，操作完成过后在把size的值写回到主存size的 

![](img\20200519004835.png)



 size的大致变化过程就是这样的，理论结果应该是size=3的，而我们实际执行的结果是size=2 



**操作源码 ++size  **

```java
/**
 * 操作
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    	........
	//这里是核心，
	++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```



**（2） resize()方法在高并发的情况下，数据丢失的情况**

```java
// 这里首次加入数据的时候 table 第一个线程的 tab = resize() 会被第二个线程覆盖  tab = resize()
if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
//代码是判断是否出现hash碰撞，假设两个线程A、B都在进行put操作，并且hash函数计算出的插入下标是相同的，当线程A执行完第六行代码后由于时间片耗尽导致被挂起，而线程B得到时间片后在该下标处插入了元素，完成了正常的插入，然后线程A获得时间片，由于之前已经进行了hash碰撞的判断，所有此时不会再进行判断，而是直接进行插入，这就导致了线程B插入的数据被线程A覆盖了，从而线程不安全
if ((p = tab[i = (n - 1) & hash]) == null) // 如果没有hash碰撞则直接插入元素
            tab[i] = newNode(hash, key, value, null);



// do while循环进行链表拼接时，由于不是原子操作，会导致拼接覆盖的情况，导致数据丢失
Node<K,V> loHead = null, loTail = null
Node<K,V> hiHead = null, hiTail = null
Node<K,V> next;                       
do {                                  
    next = e.next;                    
    if ((e.hash & oldCap) == 0) {     
        if (loTail == null)           
            loHead = e;               
        else                          
            loTail.next = e;          
        loTail = e;                   
    }                                 
    else {                            
        if (hiTail == null)           
            hiHead = e;               
        else                          
            hiTail.next = e;          
        hiTail = e;                   
    }                                 
} while ((e = next) != null);         
if (loTail != null) {                 
    loTail.next = null;               
    newTab[j] = loHead;               
}                                     
if (hiTail != null) {                 
    hiTail.next = null;               
    newTab[j + oldCap] = hiHead;      
}                                     
```



### Hash 算法

Map 是由数组构成的，key的定位算法 HashCode%n (求余算法 -- 取余数  7÷17=0....7 所以  商 0 余数 7   )

length = 2的n次幂

### 位与运算与取余  

位与也 可以用来取余 来操作 -- 但是有一个条件：除数必须是2的n次幂才行。举例子来说明

二进制计算中，众所周知的是，一个数右移1位相当于除以2的商，而恰巧被移除出去的那一位就是除以2得到的余数，例如： 

```
9 >> 1
= 1001 >> 1
= 100 | 1
= 4 余 1
```

而且，不仅是除以2，对于一个数 hash 要除以2的n次方 (length)，也就是相当于把 hash 向右移n位，而被移出去的n位即正好是我们要求是余数。 

很明显了    **对于 2的n次方 取余**   **只需要得到被除数的低n位就可以了** --( Map 就是 HashCode 的低n位 )

#### 如何获取 被除数的低n位呢？

正好，对于2的n次方这样的数 ( length ) 我们将其转换为二进制之后，它就是第n+1位为1，其余低位都为0的数，

因此我们将其减1，就得到了第n+1位为0，而其他位都为1的数

用此数( length-1 )与被除数( hash ) 进行位与运算 **(length - 1) & hash**  就得到了被除数的低n位二进制数

也即是 hashCode % length 的结果。 

**总结：**

**若一个数length满足： length = 2n 次幂**

**那么Hash % length = Hash  & (length -1)**





### HashMap --  Hash 算法

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

#### 为什么要与高16位进行异或算法呢？

上面分析到 hash 取余算法 取决于length 的变成2进制的低 n 位

那么高位就永远无法参与运算 -- 为了让其参与运算 使其更加散列 -- 让高16位和低16位进行异或算法使其参与其中

为什么是异或运算呢？与运算和或运算 得到的结果都是各75% 只有异或算法结果才是各 50%

| 与运算（都是1才1）          | 0&0  0&1  1&0  1&1            | 结果 ：0 0 0 1     | 得1概率25% 得0概率75%     |
| --------------------------- | ----------------------------- | ------------------ | ------------------------- |
| **或运算（有1就是1）**      | **0\|0   0\|1   1\|0   1\|1** | **结果 ：0 1 1 1** | **得1概率75% 得0概率25%** |
| **异或运算 （相反才是1 ）** | **0^0   0^1   1^0   1^1**     | **结果 ：0 1 1 0** | **得1概率50% 得0概率50%** |





## 扩容机制

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
    	// put 结束之后才进行扩容
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```

**扩容 是在 put 完成之后才进行判断 size 的大小 是否大于 threshold 大于 就进行扩容**

```java
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 扩容规则 扩大一倍 oldCap << 1
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    // gc 
                    oldTab[j] = null;
                    if (e.next == null)
                        // 当前数组位置对应的列表只有一个时，直接计算放入
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        // 红黑树直接转换
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        // 当前数组位置对应的列表多个
                        
                        // 元素在重新计算hash之后，因为n变为2倍，
                        // 那么元素的位置要么是在原位置，要么是在原位置 + oldCap
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            // e 当前节点
                            // next 下一个节点
                            next = e.next;
                            // 当前节点重新计算后还是原位置
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                   	// 第一次进入头节点等于本身
                                    loHead = e;
                                else
                                    // 上一个的尾节点等于当前节点
                                    loTail.next = e;
                                // 当前尾节点等于本身
                                loTail = e;
                            }
                            else {
                                // 当前节点重新计算后还是原位置 + oldCap
                                if (hiTail == null)
                                    // 第一次进入头节点等于本身
                                    hiHead = e;
                                else
                                    // 上一个的尾节点等于当前节点
                                    hiTail.next = e;
                                // 当前尾节点等于本身
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            // 元素重新算hash 在  原位置的部分
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                             // 元素重新算hash 在  原位置+ oldCap 的部分
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```



**扩容的容量规则：newCap = oldCap << 1**

**扩容后元素的位置要么是在原位置，要么是在原位置 + oldCap**

通过 判断 哈希 与 oldCap 是否等于 0（e.hash & oldCap) == 0

等于 0  放到这个链表里面   loHead = null, loTail = null;

等于1   放到这个链表里面   hiHead = null,  hiTail = null;

最后 

loTail 这个链表有值就放 原位置      --   newTab[j] = loHead;   

hiTail 这个链表有值就放 原位置+ oldCap  -- newTab[j + oldCap] = hiHead;







### 为什么扩容后 元素的位置要么是在原位置，要么是在原位置 + oldCap呢？

首先要明确  元素计算hash的位置 取觉于 cap 的2n次幂的 低n位

那么 HasmMap 扩容容量变成 原来的 两倍 newCap = oldCap * 2   （ oldCap << 1）

扩容后，因为n变为2倍，那么重新计算hash 的位置 取决因素 就比原来的多一位  高位多1bit(红色)  



![](img\20200519004837.jpg)



这个高位 观察得知要么 是 0 要么 1 ，那么 新的index就会发生这样的变化 ：

**要么是在原位置，要么是在原位置 + oldCap**

![](img\20200519004838.jpg)

#### 那么如何确定这个高位呢

原来的 n  （oldCap ）等于 1 0000  这个高位 （就是需要确定的高位）等于 1  低位都是 0

那么就可以通过 e.hash & oldCap 来判断 扩容后hash的这个高位

等于  0  这个高位 等于 0  等于  1   这个高位 等于 1

```java
扩容后的高位等于 0 就是e.hash第五位  
newCap 在扩容  hash的位置  取觉于低5位   
oldCap = 16： hash的位置   取觉于低4位  
0000 0000 0000 0000 0000 0000 0001 0000
e.hash ：    
1111 1111 1111 1111 0000 1111 0000 0101     
e.hash & oldCap
0000 0000 0000 0000 0000 0000 0000 0000  == 0
    
扩容后的高位等于 1  就是e.hash第五位     
newCap 在扩容  hash的位置  取觉于低5位   
oldCap = 16： hash的位置   取觉于低4位    
0000 0000 0000 0000 0000 0000 0001 0000
e.hash ：    
1111 1111 1111 1111 0000 1111 0001 0101  
e.hash & oldCap
0000 0000 0000 0000 0000 0000 0001 0000  == 1    
    
```











































