### 索引

索引：一种高效获取数据的  **排好序** 的 **数据结构**

索引的数据结构：

- 二叉树
- 红黑树
- hash表
- B-Tree（b树）
- B+Tree（b树的变种）

一个数据结构网站：

https://www.cs.usfca.edu/~galles/visualization/Algorithms.html



### MySql 索引有两种   B+Tree 和 HASH 

![](img\20210402114722.png)





```
KEY `index_name` (`name`) USING BTREE

KEY `index_name` (`name`) USING HASH

INDEX `index_name` (`name`)

index和key没有区别(即使有区别，也可以忽略)

index是比较标准的语法

key有primary key，foreign key，UNIQUE KEY
```





### HASH 索引是数组加链表--查找效率最高

直接hash定位，hash冲突一般也很少

缺点：只支持 “  = ”  不支持 “ in ” 和范围查找



### B-Tree （B树）

![](img\20210402120312.png)



叶节点：

每个节点横向扩展（多个--每个都包含key-data数据）

节点中的元素从左到右递增

所有的元素都是不重复的



### B+Tree （B树的变种）

![](img\20210402121041.png)



### B+树和B树的区别

非叶子节点不存储data，只存索引（冗余）

叶子节点包含所有索引数据

叶子节点有指针连接，可以提高区间访问的性能





### MySql 底层索引就是使用B+Tree

MySql 每个节点（页大小）都是16K左右 （innodb_page_size ）

```
SHOW GLOBAL STATUS like 'Innodb_page_size';

Variable_name	    Value
Innodb_page_size	16384
```

页大小：就是叶子节点



### Buffer pool 缓冲池

每次都将页大小（Innodb_page_size 16kb）放到缓冲池中--算法LRU 淘汰最少用原则





### 为什么要用B+Tree

因为：

b-tree 红黑树等每个节点一个数据（key-date）--一层就只能存少量的数据，千万数据的时候 ，会很多层（一层一次IO）

b+tree 上层节点都是不包含date的，只存key--3层就可以存千万的数据



b+tree 底层叶节点就是数据按排序好的,上面几层都是冗余的

MySql 每层叶节点 16kb

冗余节点结构：

主键(索引值 id )+下一个地址 --- 主键+下一个地址 .......

一般主键 bigInt 8字节       下一个地址：mysql底层：6个自己

16kb / (8+6) = 1142

底层叶节点结构：

主键(索引值 id )+ data (一条数据) = 数据算1k吧       

16kb /1K = 16

3层：2层冗余 + 1层数据

1142 * 1142 *16 = 20 866 624 （2千多万）

B+Tree 3层就可以存2千多万条数据

![](img\20210402124354.png)



mysql 分库不仅要看数据量还要看数据大小

主键的大小和数据的大小 （2层1304164 个数据 16kb）1304164 *16kb = 2G多空间

所以单表行数超过500万行或者单表容量超过2GB，才推荐进行分库分表



### MySql查询过程

查询有索引

找到第一层的放到内存--内存中找到下层的地址（**已排好序直接比较大小--内部二分法**）

第二层的节点找到最低层叶节点

低层叶节点就可以找到数据（叶子节点包含所有索引数据）





### mysql 存储引擎 

存储引擎是修饰表的（不是修饰数据库的）



### MyISAM 存储引擎

![](img\20210403012217.png)

在date目录下有三个文件  

- frm 表结构
- MYD data数据
- MYI 索引文件 

MyISAM 非聚集索引：（索引和数据文件分开）

- MyISAM B+树主键索引和辅助索引尾叶子节点都是 索引值和数据文件的地址指针

MYISAM 查询过程

- 如果有索引 -- 去MYI去找（B+树查询找法--MYI存储的是数据的地址指针）到地址指针
- 然后通过地址指针-去MYD文件里找数据

与Innodb相比 多了一步通过地址指针去MYD文件找数据，多一次IO，性能略差

早期的版本才支持--MyISAM 查询性能差一些，所以早期版本才支持



### InnoDB 存储引擎

![](img\20210403013038.png)

在date目录下有两个文件  

- frm 表结构
- idb 数据和索引都放到这个文件里

InnoDB 聚集索引：（索引和数据都在一个文件里面）

- InnoDB B+树主键索引叶子节点就是数据文件（尾叶子节点）
- 辅助索引（非主键索引）叶子节点是主键值

InnoDB 查询过程：

- 如果是辅助索引 -- 直接在辅助索引里找到主键（B+树查询找法）
- 然后通过--主键值找到数据（B+树查询找法）
- 如果是主键索引-- 直接通过主键值找到数据

与 MyISAM 相比 InnoDB 找数据--都在一个文件里面，少了一次IO，所以现在的表基本都是 InnoDB 的表

当然 InnoDB 支持事务是也是一方面



### 聚集索引和非聚集索引的区别

聚集索引：索引和数据都在一个文件里

缺点：

修改主键和在插入新记录的时候

如果不是在尾部，数据行必须移动到新的位置，索引此时会重排，会造成很大的资源浪费

非聚集索引：索引和数据分开放

MyISAM 用非聚集索引：

- B+树主键索引和辅助索引叶子节点都是存储数据文件的地址指针

InnoDB 用聚集索引：

- B+树主键索引叶子节点存储数据文件
- 辅助索引叶子节点存储主键值

MyISAM查询过程：

- 通过 **MYI** 文件找到地址指针
- 然后地址指针到 **MYD** 文件找到数据

InnoDB查询过程：

- 如果是辅助索引 -- 直接在辅助索引里找到主键（B+树查询找法）
- 然后通过--主键值找到数据（B+树查询找法）
- 如果是主键索引-- 直接通过主键值找到数据



### 索引分类

按存储引擎分：聚集索引和非聚集索引

按表分：主键索引、普通索引

普通索引又分：唯一索引、二级索引（就是普通索引）

按字段个数又分：单列索引和联合索引





### 面试重点

为什么 InnoDB 表必须建主键，并且推荐使用整型自增

#### 为什么表必须建主键

InnoDB 表的表如果没有主键MySql会找一个唯一列作为主键

如果没有就自己生成一列

没有主键增加MySql负担和空间浪费

所以InnoDB表必须建主键

#### 为什么推荐使用整型

UUID 作为主键-- 节点上为了排序会转成ASCII码，作为比较（内部二分法）--与整型相比没整型快

还有最主要的（16kb）的节点上存储的东西更少了（最终导致的存储数据也少了）---浪费SSD硬盘空间

#### 为什么推荐使用自增

如果是自增--B+Tree 会将数据放到尾部--自增以排好序

不会产生叶分裂

如果不是连续的插入数据的时候，当数据大于一个叶节点16kb--就会产生叶分裂

在这个叶节点中间产生一个16kb的叶节点，可能造成 一个16kb叶节点 ,就一个数据的情况，此时内部可能会造成索引重排



#### 自增ID优缺点

#### 优点：

尽管缺点很多，但都是在insert阶段

但insert之后select 性能是最好的

例如：雪花算法id ，16kb 数据占用大了，也个页节点存储得少了，IO就多了



#### 缺点 ： 

##### 可靠性不高

可以回溯，在mysql 8.0才修复这个问题

##### 安全性不高

同id 可以很容易猜测用户数

##### 性能差

自增ID性能差，在服务端生成

##### 交互多

需要额外执行 last_insert_id（）的函数才能执行插入的自增id

多一次交互，海量的并发多一个交互，数据库本来就是性能瓶颈

##### 局部唯一性

当前数据库唯一，不是全局唯一

在分布式系统中，简直是恶梦







#### B树和B+树的区别

B树的叶节点（尾叶节点）没有指针相连

B+树的叶节点有指针相连---因为有了节点有指针相连才支持范围查询

B树的节点存储data数据

B+树的叶节点才存储data数据，非叶子节点都是冗余的--因为这3层就可以存储2千多万的数据



### 什么情况下mysql索引会失效

- **不规范的使用：使用 <>， != ，Not In ，like已 '%...'开头，对字段表达式操作**

  注意：

  is null 是会走索引的 相当与常量，可以走任何索引

  col = key or col is null ，ref_or_null，优先得到 col = key 的情况（测试加上 limit ）

- **MySQL 优化器发现不用索引，更快**

  原因：查询数据占全表的比例很大-->官方文档说是 30%但不对，实际官方也说了很复杂不固定，优化器认为全表扫描更合适

- **还有一个bug  使用  order by id asc limit 1** 

  原因：优化器认为排序是个昂贵的操作，所以为了避免排序，且它**认为** limit n 的 n 

  如果很小的话即使使用全表扫描也能很快执行完，所以它选择了全表扫描

- **范围查询的时候，这个范围超过一定个数**

  解决：使用 force index ，和上面的查询数据占全表的比例很大一个道理

- **select  * from  order by 无法保证一定走索引**  

  原因：由于是 * 优化器会认为排序的代价大于全表扫描

  解决：select  字段 from  order by 字段

- **order by desc** 索引是生序，你使用的是降序

  

  

### 索引失效

两种情况：

第一种：不规范使用sql与索引

例如：sql 里面 not  <>  ，字段使用了函数，查询字段的类型不匹配，没有使用索引的最左匹配原则

第二种：优化器认为，全表扫描比走索引更快

例如：其中这也分两种情况

一种：查询的数据超过总量一定的比例

当然这30%是官方里说的，但它同时也说了，这个范围是多方面因素影响，不一定30% ，

10% 也有可能

一种：走索引确实慢的

特别体现在 select  *     order by     limit

1. select  *  + limit 很小，直接扫全表返回一条就好，limit 1 就有可能出现，limit 2 我就没发现过

2. select  *  + order by  的深分页，分页之前的数据都需要回表，这个操作是很昂贵的

   解决：不要select * ，或者 修改分页sql，传上一个最大的id，还有就是 子查询拿到数据的id，再去查

   ​			另一个使用索引（覆盖）

3. oder by 生序和降序问题，如何内存够的会，使用 filesort ，正常都会够的有1G内存可以

   但是有些机器内存就是不够，没办法会全表扫描

   解决：将索引的顺序弄对（降序高版本才有），或者加多一条对应顺序的索引，就不会使用 filesort 

   ​	        实在没办法加内存也是一种方法，调大 filesort  内存

 



1. 使用 optimizer trace 功能查看优化器生成执行计划的整个过程

```sql
SET optimizer_trace="enabled=on";        // 打开 optimizer_trace
SELECT * FROM order_info where uid = 5837661 order by id asc limit 1
SELECT * FROM information_schema.OPTIMIZER_TRACE;    // 查看执行计划表
SET optimizer_trace="enabled=off"; // 关闭 optimizer_trace
```

2. 还有一个bug  使用  order by id asc limit 1 

```
select * from order_info where uid = 5837661 order by id asc limit 1
```

​	原因: 这种基于 id 的排序写法，优化器认为排序是个昂贵的操作，所以为了避免排序，并且它**认为** limit n 的 n 

如果很小的话即使使用全表扫描也能很快执行完，所以它选择了全表扫描，也就避免了 id 的排序

（全表扫描其实就是基于 id 主键的聚簇索引的扫描，本身就是基于 id 排好序的）

这个 bug 最早追溯到 2014 年，不少人都呼吁官方及时修正这个bug，可能是实现比较困难，直到 MySQL 5.7，8.0 都还没解决，

所以在官方修复前我们要尽量避免这种写法，如果一定要用这种写法，怎么办呢，主要有两种方案

​	1.使用 force index 来强制使用指定的索引，如下：

```sql
select * from order_info force index(idx_uid_stat) where uid = 5837661 order by id asc limit 1
```

这种写法虽然可以，但不够优雅，如果这个索引被废弃了咋办？于是有了第二种比较优雅的方案

​	2. 使用 order by (id+0) 方案，如下

```sql
select * from order_info where uid = 5837661 order by (id+0) asc limit 1
```

这种方案也可以让优化器选择正确的索引，更推荐！为什么这个 trick 可以呢，因为此 SQL 虽然是按 id 排序的，

但在 id 上作了加法这样耗时的操作(虽然只是加个无用的 0，但足以骗过优化器)，优化器认为此时基于全表扫描会

更耗性能，于是会选择基于成本大小的方式来选择索引。

https://mp.weixin.qq.com/s/E65HabKRgPdWRgegGAEMnQ



### 面试误区

使用 or 不走索引的，建议使用 union all

这是有误的  or  两边都有索引还是会走索引的，使用的是索引合并（单表有效）



or 两边相同列，有索引，走索引

or 两边不同列，有索引，走索引，还是索引合并



还有 union all 有 两边的并集问题

使用 union  去重性能又达不到





联合索引: a_b 

a 的值是有序的

b的值是无序的，只有在a确定的情况下才有序（a必须等于具体值）

所以索引要遵循最左前缀匹配原则



在联合索引的范围查询的情况下会导致 mysql索引，可以使用 force_index() 强制使用索引

在 from 表名 force_index(字段) where a= a



索引：：a_b_c

a,       a,b       a,b,c    命中联合索引

ac   a可以命中联合索引（a,b,c），c无法命中   -- a可以命中，所以possible_keys列会显示使用了联合索引

最左前缀匹配原则，mysql会**一直向右匹配**直到遇到**范围查询**（>、<、between、like）就停止匹配 

联合索引在遇到范围查询，后面的索引会失效。

a的范围查询 a> ?    范围查询--辅助索引不会走索引



a like ‘%5%’ like 情况也一样



单索引：a

where a=?    ,where a> ?   辅助索引不会走索引，a是主键才会走索引



acb会走索引吗？

最左前缀匹配原则，mysql会一直向右匹配直到遇到范围查询（>、<、between、like）就停止匹配，

比如a=3 and b=4 and c>5 and d=6如果建立（a，b，c，d）顺序的索引，d是用不到索引的，如果建立（a，b，d，c）的索引则都可以用到，a，b，d的顺序可以任意调整。

=和in可以乱序，比如a=1 and b=2 and c=3建立（a，b，c）索引可以任意顺序，mysql的查询优化器会帮你优化成索引可以识别的形式



### 面试总结：mysql索引会失效

查询数据占比整表的比例大，优化器就会全表扫描



模 : 模糊查询，使用like查询 % 开头

型：数据类型错误

数：对索引字段使用内部函数

空：索引列是null

运：索引列进行四则运算

最：复合索引不按索引最左开始查找

快：全表查找预计比索引更快

不：不等于，not in 

字：字符编码不一致

注意：is null 是会走索引的





### 阿里规范关于建主键的

【强制】表必备三字段：id, create_time, update_time。

说明：其中 id 必为主键，类型为 bigint unsigned、单表时自增、步长为 1。create_time, update_time

的类型均为 datetime 类型，前者现在时表示主动式创建，后者过去分词表示被动式更新

【推荐】单表行数超过 500 万行或者单表容量超过 2GB，才推荐进行分库分表。

说明：如果预计三年后的数据量根本达不到这个级别，请不要在创建表时就分库分表。





### 前缀索引

#### 阿里规范 varchar上的索引必须要指定索引长度

**索引长度**

在varchar类型字段上通过前几位就可以确定数据的位置，这时候索引只要前几位就可以了



**一页的大小是恒定的16KB，意味着索引字段值长度占用的空间越小，一页能保存的数量也就越多，最终就体现在减少磁盘IO的次数上**

int类型占用：4byte
索引指针占用：6byte
第三层的叶子节点：16kb/1kb = 16条
第一层和第二层节点，每个节点可以保存：16kb/(4byte+6byte) ≈ 1600条

1600*1600*16 ≈ 4千万条

如果索引字段现在改成bigint类型，那么大约可以存储的数据量是：

1170*1170*16 ≈ 2千万条

可以看出，假设在树的高度不变的情况下，索引字段所占用空间的大小会直接影响数据的存储量。 



**如何确定索引长度**

需要注意的是，如果数据未匹配，也无法使用覆盖索引的特性，必须回表到聚集索引中过滤。

使用 count(distinct left(列名, 索引长度))/count(*) 的区分度，区分度会高达 90%即可



**指定索引长度后不会走覆盖索引的情况：**

如果没找到数据的唯一位置不会使用索引覆盖

```sql
drop table t_demo;

CREATE TABLE t_demo (
id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增id',
name VARCHAR(20) DEFAULT NULL COMMENT '姓名',
phone char(11) DEFAULT NULL COMMENT '手机号',
PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table t_demo add index idx_phone(phone(3));


INSERT into t_demo (name,phone) VALUES ('zz','''13900000000');
INSERT into t_demo (name,phone) VALUES ('ls','13700000000');
INSERT into t_demo (name,phone) VALUES ('ls2','13800000000');

INSERT into t_demo (name,phone) VALUES ('zz','''13900000001');
INSERT into t_demo (name,phone) VALUES ('ls','13700000002');
INSERT into t_demo (name,phone) VALUES ('ls2','13800000003');

explain select phone from t_demo where phone = '13900000001';


```

extra 列

![](img\20220914142941230.png)



**使用覆盖索引**

extra 列

![](img\20220914142941231.png)

**查索引字段区分度的方式**

```sql
SELECT count(DISTINCT LEFT(order_no, 20)) / count(*) AS '20', count(DISTINCT LEFT(order_no, 22)) / count(*) AS '22', count(DISTINCT LEFT(order_no, 24)) / count(*) AS '24', count(DISTINCT LEFT(order_no, 26)) / count(*) AS '26', count(DISTINCT LEFT(order_no, 28)) / count(*) AS '28', count(DISTINCT LEFT(order_no, 30)) / count(*) AS '30', count(DISTINCT LEFT(order_no, 32)) / count(*) AS '32' FROM test;
```

![](img\20220914142941232.png)



order_no字段长度是32，可以看出从获取长度为26开始，区分度已经接近1，再增加长度性价比已经不高了。

索引对于order_no字段设置索引长度为26比较合适



**实际**

实际上数据大都前面是相同的，只是后面几位不同，对于这样的字段我们保留前几位作为索引字段时不行的

可以使用Hash的方式，但是要注意存在Hash冲突的问题



**注意**

InnoDB 引擎单一字段索引的长度最大为 768 字节

UTF8编码三字节不能超过255 即： 768/3 =256
GBK是双字节，即：768/2=384





### 联合索引

如果有超过5个索引建议使用联合索引

**索引识别度高的放到前面**

- index a_b    a能过滤的数据多就a放前面

where执行顺序是从左往右执行的

遵守原则：

- 排除越多数据的条件放在第一个
- mysql 有优化正常情况没有影响
- 联合索引是有影响的

联合索引的数据结构：

- 第一列排序然后到第二个字段最后第三个这种顺序


![](img\20210403022358.png)



![](img\20210918170956.png)

```xml
当我们只分析a时,会发现a是有序的，1，1，2，2，3，3
当我们只分析b时,会发现b是无序的，1，2，1，4，1，2
但是如果我们先根据a排序，再来看b，就会发现在a确定的情况b其实也是有序的。
这个就是我们联合索引命中的原理。即a本身有序，在a确定的情况下（a等于具体值的情况下），b又是有序的，所以就相当于都是有序的
```



### 索引最左前缀原则

联合索引: a_b 

a 的值是有序的

b 的值是无序的，只有在a相等的情况下才有序，无法在有序的B+tree找到有序的值（通过二分法去查找）

所以索引要遵循最左前缀匹配原则 ，直接使用 b 是无法使用索引的



单索引：a

where a=?    ,where a> ?   辅助索引不会走索引，a是主键才会走索引



索引：a_b_c

范围查找右边生效原理( 范围之后的索引将会失效 )

where a>?  b=1 不走索引	a** 辅助索引找到了大于a数据的主键 ，但是去除掉了这些a之后，还有许多其他的a值绑定的b值依然是无序的

只有a 等于了某个值，b 才会是有序的

这里 a > ? ，a不是定值，b 不是有序的



where a=? 走索引   1**  

where b=? 不走索引 

where a>? 不走索引	a** 辅助索引找到了大于a数据的主键 ，但通过这些主键，后面的数据都要经历回表

这时候，mysql 会认为全表扫描比走索引更好



where a=?  b=? 走索引  a相等的情况下b值是有序的

where a=?  and b>? and c =?  走索引 ab 因为b是范围 ，没办法根据b，去做c的排序







正例：where a=? and b=? order by c; 索引：a_b_c

反例：where  b=?  索引：a_b_c  不是前缀

陷阱：索引：a_b_c     where b=？and c=? and a=? 这个也会用到索引

因为：mysql的sql引擎会优化成 where a=？and b=? and c=?



### 在order by 的场景，请注意利用索引的有序性

如：WHERE a>10 ORDER BY b; 无法排序

索引 a_b    a 存在范围查询，那么索引有序性无法利用



### 覆盖索引 

利用覆盖索引来进行查询操作，避免回表

说明：如果一本书需要知道第 11 章是什么标题，会翻开第 11 章对应的那一页吗？目录浏览一下就好，这

个目录就是起到覆盖索引的作用。



### 回表

辅助索引：找到了主键，然后在通过主键去找数据--这就是回表



### 索引下推

mysql 5.6 针对 二级索引的一项优化

```mysql
## 默认开启
SET optimizer_switch="index_condition_pushdown=on";
## 这是关闭
SET optimizer_switch="index_condition_pushdown=off";
```

`name`和`age`的联合索引为例

```text
SELECT * FROM user_innodb WHERE name = '覃%' AND age = 20;
```

`name`字段有 % ，`age`无法利用索引的顺序性来进行快速比较的

5.6之前

索引只能过滤记录都符合`name='覃%'`的条件的数据

然后回表推到Server层进行处理，过滤 age = 20 的



现在引入**索引下推**

联合索引的叶子节点中找到了`name='覃%'`的数据，而且`age` 也恰好在联合索引的叶子节点的记录中

这个时候可以直接在联合索引的叶子节点中进行遍历，筛选出age = 20 的数据

最后找到符合数据的 主键值

只需要进行1次回表操作即可找到符合全部条件

```mysql
EXPLAIN SELECT * FROM user_innodb WHERE name = '覃%' AND age = 20;
```

执行以下SQL语句，并用`EXPLAIN`查看一下执行计划，此时的执行计划是`Using index condition`

![](img\20220914142941239.png)



关闭 索引下推

```mysql
SET optimizer_switch="index_condition_pushdown=off";
```

```mysql
EXPLAIN SELECT * FROM user_innodb WHERE name = '覃%' AND age = 20;
```

再次执行查询语句，并用EXPLAIN查看一下执行计划，此时的执行计划是`Using where`

![](E:\GitHub\base\mysql\img\20220914142941240.png)



### 隐藏索引

**MySQL8.0** 出现的功能：

主要解决问题：不改变实际索引结构的情况下，测试删除索引对于查询性能的影响

因为大表删除并重建索引的开销很大

索引是否隐藏不影响索引的使用，无论索引是否隐藏。当表数据更改时，索引仍然会进行更改，唯一索引仍然会阻止重复值的插入。

**控制隐藏索引是否可用**

可以通过optimizer_switch的use_invisible_indexes标识来控制优化器是否使用隐藏索引。

该标识默认为off，优化器将忽略隐藏索引。如果标识为on，即便索引是隐藏的，优化器也会使用该索引。





**创建隐藏索引**

使用 VISIBLE 或者 INVISIBLE 子句来控制索引是否隐藏。

```sql
#示例
CREATE TABLE t1 (
  i INT,
  j INT,
  k INT,
  INDEX i_idx (i) INVISIBLE
) ENGINE = InnoDB;
CREATE INDEX j_idx ON t1 (j) INVISIBLE;
ALTER TABLE t1 ADD INDEX k_idx (k) INVISIBLE;

ALTER TABLE t1 ALTER INDEX i_idx INVISIBLE;
ALTER TABLE t1 ALTER INDEX i_idx VISIBLE;
```



**查看隐藏索引**

索引是否隐藏可以通过SHOW INDEX或者查询INFORMATION_SCHEMA.STATISTICS表来进行查看

```sql
SELECT INDEX_NAME, IS_VISIBLE
       FROM INFORMATION_SCHEMA.STATISTICS
       WHERE TABLE_SCHEMA = 'db1' AND TABLE_NAME = 't1';
+------------+------------+
| INDEX_NAME | IS_VISIBLE |
+------------+------------+
| i_idx      | YES        |
| j_idx      | NO         |
| k_idx      | NO         |
+------------+------------+
```







### 降序索引

mysql 8.0 默认升序排序的 asc

例如：员工ID 正排，时间倒排 

那么我们就可以 将时间设置为 降序索引

这种情况在 order by 的情况下很常见

```mysql
create table t (
	id int auto_increment primary key ,
    c1 int ,
    c2 int ,
	index(c1 asc, c2 desc)
);



explain select * from t order by c1 asc ,c2 asc


'1', 'SIMPLE', 't', NULL, 'index', NULL, 'c1', '10', NULL, '458382', '100.00', 'Using index; Using filesort'

```



Using index; Using filesort

表示：索引之外还需要额外的排序



```mysql
explain select * from t order by c1 asc ,c2 desc


1	SIMPLE	t		index		c1	10		458382	100.00	Using index
```



Using index

这时候就不需要额外的排序



### 查看索引是否倒序

SHOW INDEX FROM TABLE

Collation   = D  就是倒序索引   A 为升序

+-----------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| Table     | Non_unique | Key_name | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment | Visible | Expression |
+-----------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| t_article |          0 | PRIMARY  |            1 | id          | D         |           0 | NULL     | NULL   |      | BTREE      |         |               | YES     | NULL       |
+-----------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+





### 函数索引

MySQL 8.0.13 之前不支持函数索引

小写函数：创建索引 -- email 字段小写唯一的约束

```sql
CREATE unique index test1_idx ON test1 (lower(email));
```



explain select * from test1 where  lower(email) = lower('QinJP@qq.com')

这时候，email就可以使用索引了



在 JSON 类型的字段 和 在 DATETIME 类型 ，条件又是函数 date 执行的 很常见

#### 函数索引 处理 DATETIME 类型 ，又函数 date 执行的 

```sql

CREATE TABLE `t1` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `log_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_log_time` (`log_time`)
) ENGINE=InnoDB ;

select * from t1 where date(log_time) = '2023-06-04';




explain select * from t1 where date(log_time) = '2123-06-04';

'1', 'SIMPLE', 't1', NULL, 'index', NULL, 'idx_log_time', '9', NULL, '139', '100.00', 'Using where; Using index'

虽然走了索引，但是扫描行数为总记录数，相当于全表扫

为列 log_time 加一新索引

alter table t1 add key idx_func_index_1((date(log_time)));

再次执行上面的 SQL 1，瞬间执行完毕。

explain select * from t1 where date(log_time) = '2123-06-04';

'1', 'SIMPLE', 't1', NULL, 'ref', 'idx_func_index_1', 'idx_func_index_1', '4', 'const', '1', '100.00', NULL

```





#### **函数索引 处理 JSON 类型**

 JSON 类型 不能创建索引

但可以创建函数索引

```sql
create table t2 (id int primary key, r1 json);


alter table t2 add key idx_func_index_2((cast(r1->>'$.name' as char(30)) collate utf8mb4_bin));

select * from t2 where r1->>'$.name'='qinjp';
```





### 重复索引与冗余索引

重复索引：表示一个列或者顺序相同的几个列上建立的多个索引。

冗余索引：两个索引所覆盖的列重叠

**场景**

比如文章与标签表

```sql
CREATE TABLE `t16` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `artid` int(10) unsigned NOT NULL DEFAULT '0',
  `tag` char(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `at` (`artid`,`tag`),
  KEY `ta` (`tag`,`artid`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8
```

这个表中有两个索引，一个是at，一个是ta，两个索引都用到了artid和tag两个字段。

业务SQL语句：

select tag，artid   from t11 where artid=2;

select tag，artid   from t11 where tag=’PHP’;

这个业务场景 重复索引与冗余索引 可以覆盖到了 tag 和  artid 不用回表



### 两个单列索引，不如一个联合索引

冗余一个联合索引





### 索引合并

or 两边相同列，有索引，走索引

or 两边不同列，有索引，走索引，还是索引合并

type = index_merge

缺点：只有单表有效



















































































