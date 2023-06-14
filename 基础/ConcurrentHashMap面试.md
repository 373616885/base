### ConcurrentHashMap（肯课为HashMap）如何保证线程安全



ConcurrentHashMap 的整体架构和HashMap是一样的

都是数组，链表，红黑树组成的



功能在HashMap的基础上提供了并发安全的实现

并发安全的实现主要通过对数组的 node 节点去加锁，来保证数据更新的安全

锁住Node之前的操作是基于在volatile和CAS 去计算位置的位置的

在initTable ， tabAt ，casTabAt，setTabAt

通过synchronized 锁住Node，然后去更新数据，保证数据安全



性能做了优化：

1.7 锁的是 Segment ,分段锁

1.8 锁的是Node 节点，锁的力度颗粒更小



红黑树降低查询复杂度，O log n



扩容：

引人多线程并发扩容的实现

简单：多线程对原始数据进行分片，每个线程负责自己的分片去扩容，减少竞争



获取 size 

性能不激烈的时候，通过CAS去递增

性能激烈的时候，引入数组，线程随机对数组进行递增，减少并发，最后计算所有数组个数之和





















