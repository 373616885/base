### redis是单线程还是多线程

redis5.0之前是  单线程worker 

单worker线程：即处理IO网络请求（TCP），也操作内存（对内存的操作）

![](img\20210408223600.png)



![](img\20210408224755.png)



redis6.0之后  单线程worker 不处理IO网络请求（TCP），只操作内存

IO网络请求有专门的IO子线程

![](img\20210408230158.png)



![](img\20210408231039.png)



redis6.0之后是   IO网络请求有专门的IO子线程   性能更好





### Redis缓存数据机制

redis 保存数据磁盘有两种方式，两种数据文件格式

AOF:  记录会以append追加到AOF文件里面去，中间的操作过程也追加到AOF文件里

   	   最终有效的是最后的数据--过程中的数据是不可取的，AOF不断变大，

​          超过设置的阈值（64M）就会rewrite

​          rewrite作用将过程化的数据都去掉，只保留最终的数据

​		 AOF文件就会瘦身变小

​         AOF文件大，数据不易丢失，恢复慢，耗性能

RDB：某个时间点整个内存的快照--数据有丢失

​       RDB 文件小，数据易丢失，恢复快，性能较好



redis4.0 之后提供一个混合持久化方式

这是推荐的使用方式：

持久化文件既有RDB也有AOF

当AOF，rewrite的时候，删除整个文件，保存RDB，然后在后面append , AOF

整个文件头是RDB后面都是追加的AOF



RDB 是 Redis 默认的持久化方式



### Redis 线程模型

reids 基于Reactor （响应式）模式开发的网络事件处理器，单线程的，IO多路复用



###  Redis为什么快

1. 纯内存操作
2. 网络IO请求基于非阻塞的IO多路复用
3. worker单线程避免的多线程的频繁上下文切换



### Redis 单线程有什么缺点

会产生阻塞，在高并发下不应该存储大key，大value

大key，大value ，使用异步处理方案，提高带宽，多主同步



### 缓存雪崩

大量的key在同一时刻大面积缓存失效

解决：

1. 缓存加上随机数-错开



### 缓存穿透

缓存和数据库中都没有的数据，例如：id为“-1”

解决：

1. 接口层增加数据校验
2. 取不到的数据也缓存
3. 布隆过滤器



### 缓存击穿

某个 热点 key 缓存时间到期，大量并发过来

解决：

1. 加锁，设置缓存的时候加锁
2. 设置key永不过期，后台异步修改缓存



###  缓存问题

设置热点数据永远不过期，异步去修改缓存



### 内存淘汰策略

**删除过期键策略**

Redis 将使用两种策略来删除这些过期键，它们分别是惰性删除和定期删除。

惰性删除：不主动删除过期的键值，而是当访问键值时，再检查是否需要删除

定期删除：每隔一段时间会检查一下数据库，看看是否有过期键可以被清除



Redis 定期检查的频率是每秒扫描 10 次，用于定期清除过期键。

当然此值还可以通过配置文件进行设置，在 redis.conf 中修改配置“hz”即可，默认的值为“hz 10”



**内存满了淘汰策略**

在 4.0 版本之前 Redis 的内存淘汰策略有以下 6 种。

no-enviction

不淘汰任何数据，不可写入任何数据集，所有引起申请内存的命令会报错



allkeys-lru

淘汰整个键值中最近最少使用的键值。(最后一次访问时间最小)

allkeys-random

从数据集中任意选择数据淘汰 



volatile-lru 

淘汰所有设置了过期时间的键值中最近最少使用的键值。(最后一次访问时间最小)

volatile-random：

随机淘汰设置了过期时间的任意键值。



volatile-ttl 

已设置过期时间的数据集中挑选将要过期的数据淘汰



Redis 4.0 版本中又新增了 2 种淘汰策略：

volatile-lfu

淘汰所有设置了过期时间的键值中最不常用的的键值；（总访问次数来淘汰数据的）

allkeys-lfu

淘汰整个键值中最不常用的键值。（总访问次数来淘汰数据的）



allkeys-xxx 表示从所有的键值中淘汰数据，而 volatile-xxx 表示从设置了过期键的键值中淘汰数据。



redis.conf 对应的配置项是“maxmemory-policy noeviction”，只需要把它修改成我们需要设置的类型即可

或者

命令行工具输入 “config set maxmemory-policy noeviction”来修改内存淘汰的策略

马上就会生效，无需重启 Redis 服务器，但重启 Redis 服务器之后设置的内存淘汰策略就会丢失



（1）LRU（ Least Recently Used，最近最少使用）淘汰算法：是一种常用的页面置换算法，也就是说最久没有使用的缓存将会被淘汰。

LRU 是基于链表结构实现的，链表中的元素按照操作顺序从前往后排列，最新操作的键会被移动到表头，当需要进行内存淘汰时，只需要删除链表尾部的元素即可。

Redis 使用的是一种近似 LRU 算法，目的是为了更好的节约内存，它的实现方式是给现有的数据结构添加一个额外的字段，用于记录此键值的最后一次访问时间。Redis 内存淘汰时，会使用随机采样的方式来淘汰数据，它是随机取 5 个值 (此值可配置) ，然后淘汰最久没有使用的数据。

（2）LFU（Least Frequently Used，最不常用的）淘汰算法：最不常用的算法是根据总访问次数来淘汰数据的，它的核心思想是“如果数据过去被访问多次，那么将来被访问的频率也更高”。

LFU 相对来说比 LRU 更“智能”，因为它解决了使用频率很低的缓存，只是最近被访问了一次就不会被删除的问题。如果是使用 LRU 类似这种情况数据是不会被删除的，而使用 LFU 的话，这个数据就会被删除。Redis 内存淘汰策略使用了 LFU 和近 LRU 的淘汰算法，具体使用哪种淘汰算法，要看服务器是如何设置内存淘汰策略的，也就是要看“maxmemory-policy”的值是如何设置的。





### Redis核心对象

reids中定义了一个数据结构用来统一表示各种数据类型，它叫做redisObject

```objectivec
typedef struct redisObject {
    unsigned type:4;    //记录数据值的类型:string、list、hash、set、zset
    unsigned encoding:4;    //记录数据值的编码格式
    unsigned lru:LRU_BITS;  //记录操作时间,当redis内存超限时,该值可辅助lru算法清理数据
    int refcount;   //记录当前对象被引用的次数
    void *ptr;    //记录存储数据位置的指针
} robj;
```



### Redis数据类型

截止到redis6版本，一共定义了以下七种基本数据类型

```objectivec
#define OBJ_STRING 0    /* String object. */
#define OBJ_LIST 1      /* List object. */
#define OBJ_SET 2       /* Set object. */
#define OBJ_ZSET 3      /* Sorted set object. */
#define OBJ_HASH 4      /* Hash object. */
#define OBJ_MODULE 5    /* Module object. */  下面这两个数据类型是之前没有的
#define OBJ_STREAM 6    /* Stream object. */
```

可通过命令type key来查看数据值的所属类型

```shell
127.0.0.1:0>set name qinjp
OK

127.0.0.1:0>type name
string
```



### SDS

Redis 的字符串存储并没有采用c原生的字符串，而是自己定义字符串的结构定义

叫做 SDS(simple dynamic String) 简单动态字符串。

sds.h 源码文件中

```objectivec
/* Note: sdshdr5 is never used, we just access the flags byte directly.
 * However is here to document the layout of type 5 SDS strings. */
struct __attribute__ ((__packed__)) sdshdr5 {
    unsigned char flags; /* 3 lsb of type, and 5 msb of string length */
    char buf[];
};
struct __attribute__ ((__packed__)) sdshdr8 {
    uint8_t len; /* used */                                         //字符串长度
    uint8_t alloc; /* excluding the header and null terminator */   //分配内存的大小
    unsigned char flags; /* 3 lsb of type, 5 unused bits */         //标志位
    char buf[];                                                     //字符数组
};
struct __attribute__ ((__packed__)) sdshdr16 {
    uint16_t len; /* used */
    uint16_t alloc; /* excluding the header and null terminator */
    unsigned char flags; /* 3 lsb of type, 5 unused bits */
    char buf[];
};
struct __attribute__ ((__packed__)) sdshdr32 {
    uint32_t len; /* used */
    uint32_t alloc; /* excluding the header and null terminator */
    unsigned char flags; /* 3 lsb of type, 5 unused bits */
    char buf[];
};
struct __attribute__ ((__packed__)) sdshdr64 {
    uint64_t len; /* used */
    uint64_t alloc; /* excluding the header and null terminator */
    unsigned char flags; /* 3 lsb of type, 5 unused bits */
    char buf[];
};
```

`sds`的结构 定义了5种，除 **sdshdr5** 外其他的四种结构是一样的，**sdshdr5** 可以不用看，

因为在代码实现中判断如果需要使用**sdshdr5l**来存储代码会直接将其替换成**sdshdr8**。

sds 结构的4个字段

1. **buf[]**：字符串，真正存储的位置
2. **len**：当前 buf 数组中存储字符串的最大长度
3. **alloc**：这个 buf数组最大能够存储多长的字符串
4. **flags**：表示 sds 的类型



SDS 结构在 len 和 alloc 的定义单位上不同

![](img\2020023103.webp)

优点：根据字符串长度，去匹配类型





### Redis编码格式

所有编码格式

```objectivec
#define OBJ_ENCODING_RAW 0     /* Raw representation */
#define OBJ_ENCODING_INT 1     /* Encoded as integer */
#define OBJ_ENCODING_HT 2      /* Encoded as hash table */
#define OBJ_ENCODING_ZIPMAP 3  /* Encoded as zipmap */
#define OBJ_ENCODING_LINKEDLIST 4 /* No longer used: old list encoding. */
#define OBJ_ENCODING_ZIPLIST 5 /* Encoded as ziplist */
#define OBJ_ENCODING_INTSET 6  /* Encoded as intset */
#define OBJ_ENCODING_SKIPLIST 7  /* Encoded as skiplist */
#define OBJ_ENCODING_EMBSTR 8  /* Embedded sds string encoding */
#define OBJ_ENCODING_QUICKLIST 9 /* Encoded as linked list of listpacks */
#define OBJ_ENCODING_STREAM 10 /* Encoded as a radix tree of listpacks */
#define OBJ_ENCODING_LISTPACK 11 /* Encoded as a listpack */
```



### String 编码格式

最大允许key大小为512M



Redis 中的字符串有三种编码格式。分别是 int、embstr、raw

可通过命令object encoding key来查看数据值使用的编码格式

```shell
127.0.0.1:0>object encoding name
embstr

127.0.0.1:0>object encoding lname
raw
```



#### **int格式**

当数据值为数字且长度小于20时，就会采用int格式进行存储

#### **embstr格式**--短字符串

当数据值为数字且长度大于20时，或者数据值为字符串且长度不大于44时，就会采用embstr格式进行存储

**该种编码格式是将sds字符串与其对应的redisObject对象分配在同一块连续的内存空间**

类似于这种结构：

![](img\20220914142941233.png)



#### **raw格式**--长字符串

当字符串长度大于44时，将会使用raw编码格式，SDS字符串结构体的内存跟其对应的redisObject对象的内存也不一定是连续的了



### List 编码格式

quicklist 编码格式

在3.2版本之前，Redis采用ZipList和LinkedList来实现List，当元素数量小于512并且元素大小小于64字节时采用ZipList编码，超过则采用LinkedList编码。

在3.2版本之后，Redis统一采用QuickList来实现List：



#### quicklist 示意图

quicklist是由多个ziplist组成的双向链表

![](img\20220914142941234.png)

#### ziplist示意图

ziplist是一块连续的内存地址，他们之间无需持有prev和next指针，能通过地址顺序寻址访问

![](img\20220914142941235.png)



### Hash 编码格式

hash 内部有两种编码格式： **ziplist**和**hashtable**。

**ziplist**：个数比较少  （512）

**hashtable**：个数较多



### Set  编码格式

set 内部有两种编码格式：**intset**和**hashtable**

**intset**：个数比较少 （512）

**hashtable**：个数较多



### Zset 编码格式

有个分数排序，**zadd key score member**

set 内部有两种编码格式：**ziplist**和**hashtable**

**ziplist**：个数比较少 （128）

**hashtable**：个数较多





### **Redis 集群的**

- 主从复制模式
- Sentinel（哨兵）模式
- Cluster 模式



#### 主从复制模式

一个主 redis（master）多个从 redis (slave）

原理：

1. 从数据库启动成功后，连接主数据库，发送 SYNC 命令；
2. 主数据库接收到 SYNC 命令后，开始执行 BGSAVE 命令生成 RDB 文件并使用缓冲区记录
3. 主数据库 BGSAVE 执行完后，向所有从数据库发送快照文件
4. 从数据库收到快照文件后丢弃所有旧数据，载入收到的快照；
5. 从数据库完成对快照的载入，开始接收主数据库的写命令请求
6. **从数据库初始化完成**
7. 主数据库每执行一个写命令就会向从数据库发送相同的写命令

总结：主从刚刚连接的时候，进行全量同步；全同步结束后，进行增量同步。



**缺点**

1. Redis不具备自动容错和恢复功能（**主宕机要人工介入**）
2. 多个 Slave 重启，会导致 Master IO 剧增从而宕机



#### **Sentinel（哨兵）模式**

哨兵就相当于对主从服务器做一个监视的任务。一旦发现主服务器宕机了，就迅速启动相应的规则将某一台从服务器升级为主服务器，无需人工干预，更稳定更快。

**哨兵是一个独立的进程，独立运行**

![](img\20220914142941236.png)

多个 `Sentinel` 监听到一个主服务器节点

![](img\20220914142941237.png)

原理：

Sentinel（哨兵）进程以每秒钟一次的频率向整个集群中的 Master 主服务器，Slave 从服务器以及其他Sentinel（哨兵）进程发送一个 PING 命令

如果一个实例（instance）距离最后一次有效回复 PING 命令的时间超过 down-after-milliseconds 选项所指定的值， 

则这个实例会被 Sentinel（哨兵）进程标记为主观下线（SDOWN）



总结：**可以看作自动版的主从复制**



### **Cluster 集群模式（Redis官方）**

Redis 的哨兵模式基本已经可以实现高可用，读写分离 ，但是在这种模式下每台 Redis 服务器都存储相同的数据，很浪费内存

所以在 redis3.0上加入了 Cluster 集群模式，实现了 Redis 的分布式存储，**也就是说每台 Redis 节点上存储不同的内容**



使用：**集群的数据分片**

引入了哈希槽【hash slot】的概念

Redis 集群有16384 个哈希槽，每个 key 通过 CRC16 校验后对 16384 取模来决定放置哪个槽。

集群的每个节点负责一部分hash槽

举个例子，比如当前集群有3个节点，那么：

- 节点 A 包含 0 到 5460 号哈希槽
- 节点 B 包含 5461 到 10922 号哈希槽
- 节点 C 包含 10923 到 16383 号哈希槽

添加个节点 D 

从节点 A， B， C 中移除部分槽到 D 上



### redis执行过程

客户端申请连接

服务端使用 IO 复用，有专门的连接管理器对连接进行管理

客户端按协议发送指令

服务端收到指令

服务端解析命令，通过命令表查找对应的命令

找到后，执行前的准备：命令校验，参数校验，权限校验，内存检测，其他校验

调用命令实现函数

执行后续操作：慢日志记录、redisCommand结构属性更新、AOF持久化记录、主从复制命令传播等。



服务端将命令回复发送客户端


客户端接收