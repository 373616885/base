### 优化目的

**最主要优化 减少IO**

mysql连接超时

慢查询导致阻塞

阻塞导致无法提交



### 优化从几方面进行

- sql及索引
- 数据库表结构
- 系统配置
- 硬件

![](img\20210403095919.png)





### Sakila 数据库

 https://dev.mysql.com/doc/index-other.html 这个页面下载

 http://downloads.mysql.com/docs/sakila-db.zip 直接下载



### 开启慢查询

![](img\20210403102204.png)

```shell
SHOW VARIABLES LIKE 'slow%'

SHOW VARIABLES LIKE 'slow_query_log'
SHOW VARIABLES LIKE 'slow_query_log_file'

SHOW VARIABLES LIKE 'long_query_time'

SHOW VARIABLES LIKE 'log%'
 
## 开启没有用索引的查询
SET GLOBAL log_queries_not_using_indexes=ON
## 设置查询时间多大是慢查询
SET GLOBAL long_query_time=2
## 开启慢查询
SET GLOBAL slow_query_log=ON
```



### 慢查询日志的存储格式

```
# Time: 2021-04-03T02:57:17.130262Z
# User@Host: root[root] @  [115.44.109.28]  Id: 92241
# Query_time: 0.647522  Lock_time: 0.000281 Rows_sent: 200  Rows_examined: 322998
SET timestamp=1617418636;
SELECT * FROM `actor_info` LIMIT 0, 1000;
```

日志的存储格式五部分：

Time: 执行时间

User@Host：执行sql用户

Query_time：sql执行信息

SET timestamp：sql执行时间戳

SQL的内容：SELECT * FROM `actor_info` LIMIT 0, 1000;



### show profiles 查看性能问题

select 之后使用 ：show profiles 查看可以查看性能

```sql
show profiles
```

**开启profile**

```sql
 set profiling=1;
```

**查看拿到 Query_ID**

```sql
show profiles ;
```

 **`show profile cpu for query Query_ID` 即可查询到 SQL 语句资源使用情况**

```sql
-- 查看CPU指标
show profile cpu for query 2

-- 查看所有指标
show profile ALL for query 2
```





### MySQL慢查日志分析工具

pt-query-digest



### show warnings

show warnings 

查看explain优化后的语句

navicat  不显示，使用命令行或者 mysql workbench



### explian analyze

除了，显示执行计划外，还显示额外的信息

```sql
EXPLAIN (ANALYZE) SELECT count(*) FROM c WHERE pid = 1 AND cid > 200;
```

显示实际执行时间（以毫秒为单位）、实际行数和显示该节点执行频率的循环计数。

它还显示过滤器已删除的行数

注意：从后面开始看起



### explian (analyze, buffers)

如果`track_io_timing = on`

```sql
EXPLAIN (ANALYZE, BUFFERS) SELECT count(*) FROM c WHERE pid = 1 AND cid > 200;
```

显示每个节点在缓存（hit）中找到的数据块数量、从磁盘读取的数据块数量、写入的数据块数量以及被脏读的数据块数量

### explain



![](img\20220914142941250.png)



![](img\20210403131324.png)



### select_type

一般都是SIMPLE （没有使用union或者子查询）





### type

const：一般是主键，唯一索引查找

eq_reg: 一般是主键，唯一索引的范围查找

ref:  某个索引的查找

range: 某个索引的范围查找

index：索引的扫描

ALL: 全表扫面



![](img\20210403132130.png)



### extra

**using filesort一般在order by 中**

**using temporary:用了临时表**

这两个是需要优化的

**using index** 这是索引覆盖最好的

**using where** ：服务层进行where条件过滤



【推荐】如果有 order by 的场景，请注意利用索引的有序性。order by 最后的字段是组合索

引的一部分，并且放在索引组合顺序的最后，避免出现 file_sort 的情况，影响查询性能。

正例：where a=? and b=? order by c; 索引：a_b_c

【推荐】利用覆盖索引来进行查询操作，避免回表。

说明：如果一本书需要知道第 11 章是什么标题，会翻开第 11 章对应的那一页吗？目录浏览一下就好，这

个目录就是起到覆盖索引的作用。

正例：能够建立索引的种类分为主键索引、唯一索引、普通索引三种，而覆盖索引只是一种查询的一种效

果，用 explain 的结果，extra 列会出现：using index。





### SQL判断是否"存在"

count(*) 判断存在与否

```java
#### SQL写法:
SELECT count(*) FROM table WHERE a = 1 AND b = 2

#### Java写法:
int nums = xxDao.countXxxxByXxx(params);
if ( nums > 0 ) {
  //当存在时，执行这里的代码
} else {
  //当不存在时，执行这里的代码
}
```

优化方案

```java
#### SQL写法:
SELECT 1 FROM table WHERE a = 1 AND b = 2 LIMIT 1

#### Java写法:
Integer exist = xxDao.existXxxxByXxx(params);
if ( exist != NULL ) {
  //当存在时，执行这里的代码
} else {
  //当不存在时，执行这里的代码
}
```



### 特别注意：字符串类型的字段 where 等于 数字（隐式转换）

这种情况很容易不走索引，隐式转换 ，特别需要注意很容易忽略



### 子查询的优化

一般子查询都改成join 但需要注意一对多关系，要注意重复数据



因为子查询需要执行两次数据库查询，一次是外部查询，一次是嵌套子查询。

因此，使用连接查询可以减少数据库查询的次数，提高查询的效率。

另外连接查询可以更好地利用数据库索引，提高查询的性能。



子查询通常会使用临时表或内存表，而连接查询可以直接利用表上的索引。

这意味着连接查询可以更快地访问表中的数据，减少查询的资源消耗。





### 强制走索引

查询数据占比整表的比例大，优化器就会全表扫描

force_index 函数

select order_key ,createtime FROM aaa force index(createtime) group by order_key



### 优化LIMIT

没有在索引或者主键进行oreder by排序 会使用 using filesort

1. 联合降序（或者默认生序）索引进行oreder by排序 去除 using filesort
2. 子查询拿到 id ，然后用id 去查询
3. 将上一条的最大id传入，用id去过滤



### 索引优化

**联合索引：识别度高的放到前面**

![](img\20210403163505.png)

**查找重复冗余索引**

重复冗余索引：多个前缀相同或者联合索引中包含了主键的索引

```sql
方法一：通过MySQL的information_schema数据库 查找重复与冗余索引

SELECT a.table_schema AS '数据库', a.table_name AS '表名', a.index_name AS '索引1', b.index_name AS '索引2', a.column_name AS '重复列名'
FROM information_schema.statistics a
    JOIN statistics b ON a.table_schema = b.table_schema
        AND a.table_name = b.table_name
        AND a.seq_in_index = b.seq_in_index
        AND a.column_name = b.column_name
WHERE a.seq_in_index = 1
    AND a.index_name != b.index_name
    
方法二：通过工具
用pt-duplicate-key-checker 工具检查重复及冗余索引
使用方法 pt-duplicate-key-checker -hxxx -uxxx -pxxx 
```



### 删除不需要的索引

注意：主从里面需要主从两个数据库都要分析--不要漏了



### 表结构优化

1. 表要有主键--自增整型
2. 尽量不要有外键
3. 符合范式--加上唯一索引
4. 数据类型--选择合适的
5. 尽可能使用 not null 并给默认值
6. 少用text类型大字段--非用不可能最好分表--将大字段拆分出来（垂直拆分）



时间使用int来存储利用FROM_UNIXTIME（）和 UNIX_TIMESTAMP 相互转换

```sql
SELECT UNIX_TIMESTAMP('2021-04-03 11:12:11') from dual;

SELECT FROM_UNIXTIME(1617419531)
```

使用BigInt来保存IP地址，

```sql
SELECT INET_ATON('192.168.1.1') 

SELECT INET_NTOA(3232235777)
```



### 范式化

范式化一般是指数据库设计的规范，现在一般都是指第三范式

不存在关键字段对任意字段的传递函数依赖

| 商品名称（主键） | 价格 | 重量  | 分类 | 分类描述 |
| ---------------- | ---- | ----- | ---- | -------- |
| 可乐             | 3.00 | 250ml | 饮料 | 碳酸饮料 |
| 百事             | 3.00 | 250ml | 饮料 | 碳酸饮料 |
| 芬达             | 3.00 | 250ml | 饮料 | 碳酸饮料 |

存在 以下传递函数依赖关系

商品名称 -->分类 -->分类描述

分类描述 对关键字段 商品名称 存在传递函数依赖

不符合范式：

不符合范式会造成数据冗余，但为了查询效率的目的，可以适当冗余（空间换时间）



### 表的垂直拆分

1. 将不常用的字段放到一个表中
2. 将大字段放到一个表中
3. 将经常使用的字段放到一个表中



### 表的水平拆分

ID 进行HASH 运算，对齐 除模取余



### 系统配置优化

- 增加tcp连接数
- 增加打开文件句柄数
- 最好关闭软件防火墙
- 配置文件参数的调整

![](img\20210403205723.png)

![](img\20210403205848.png)



### MySql 配置文件

Liunx 大多数情况下文件在 /etc/my.cnf或者etc/mysql/my.cnf

windows 大多数在 c:/windows/my.cnf

查找配置文件的顺序

```shell
先查看mysqld服务进程所在位置:
which mysqld
使用mysqld命令执行
mysqld --verbose --help | grep -A 1 'Default options'
```

注意如果多个位置存在配置文件，则后面的会覆盖前面的



### 常用参数

**innodb_buffer_pool_size**  : 缓存池大小
推荐使用内存的60-80%



**innodb_flush_log_at_trx_commit**:多长时间变更到磁盘

参数 0 1 2 三个值 默认值 1

0 在事务提交的时候，不会主动触发写入磁盘的操作，每秒一次地将log buffer写入log file中，并且log file的flush(刷到磁盘)

1 每次事务提交时MySQL都会把log buffer的数据写入log file，并且flush(刷到磁盘)

2 每次事务提交时MySQL都会把log buffer的数据写入log file，但是不会flush(刷到磁盘)操作

注意事项：

0 速度最快但会导致上一秒钟所有事务数据的丢失

1 该模式是最安全的， 但也是最慢的一种方式

2 该模式速度较快，也比0安全，只有在操作系统崩溃或者系统断电的情况下，上一秒钟所有事务数据才可能丢失



**innodb_file_per_table** : 控制每一个表使用独立的表空间，默认OFF 

打开这个参数--避免表的锁的竞争



**innodb_stats_on_metadata**: 刷新表的统计信息



修改 filesort 内存块大小（面试使用，没人会改）

内存大了，文件块就少，IO就少



修改 sort_buffer 排序行数大小（面试使用，没人会改）

使用固定的排序行数大小去文件块就少，就可以减少IO



sort_buffer

sort_buffer_size



当前连接数（Threads_connected）

最大连接数（max_connections）



### 应用层优化建议

1. 尽可能让所有数据检索都通过索引来完成，避免无索引行锁升级为表锁

2. 合理设计索引，尽量缩小锁的范围

3. 尽可能较少检索条件，避免间隙锁

4. 尽量控制事务大小，减少锁定资源量和时间长度

5. 尽可能低级别事务隔离





 ### 初级索引调优

当 period = '202302'  数据很多，优化器容易认为不如全表扫描，就没有走索引 period

但如果走索引，后面的 limit 10 是可以很快拿到数据的

```sql
select * from order_info where period = '202302' order by modified desc limit 0,10
```

优化使用 force index 走强制索引

```sql
select * from order_info force index(period_index)  where period = '202302' order by modified desc limit 0,10
```



### 中级索引优化

force index 是不推荐的，后面还有  **using filesort**



联合降序（或者默认生序）索引：去除 **using filesort**

```sql
alter table order_info add key idx_period_modified(period, modified desc);


select * from order_info force index(period_index)  where period = '202302' order by modified desc limit 0,10
```





### 高级索引优化

经过上面优化还有 limit 深分页的问题

深分页主要问题：需要拿到前面页的所有数据，都要进行回表

解决：在索引上拿到 id ，然后使用ID去拿数据，避免了前面的数据回表



**IN获取id**

```sql
select * from table_name where id in (select id from table_name where user = xxx ) limit 6000000, 10;
```

**join方式 + 覆盖索引（推荐）**

```sql
select * from table_name inner join ( select id from table_name where user = xxx limit 6000000,10) as temp where temp.id = table_name.id




select * from table_name t1, (select id from table_name order by user limit 6000000, 100) t2  WHERE t1.id = t2.id;

```



**最推荐**

业务上传之前最大的id

```sql
select * from table_name where id > 10000 limit 10
```



### 优化案例

```sql
CREATE TABLE orders (
    o_orderkey INT,
    o_custkey INT,
    o_orderstatus CHAR(1),
    o_totalprice DECIMAL(15 , 2 ),
    o_orderdate DATE,
    o_orderpriority CHAR(15),
    o_clerk CHAR(15),
    o_shippriority INT,
    o_comment VARCHAR(79),
    PRIMARY KEY (o_orderkey)
)  ENGINE=INNODB;
```

sql  语句

```sql
select * from  orders
where o_orderdate > '2022-01-01' 
and ( o_orderpriority = 1 or o_shippriority = 1)
order by o_orderdate desc
limit 20,10;

-- 订单优先级 o_orderpriority  ， 发货优先级 o_shippriority
-- 数据在几十万左右 时间均匀分布在 2021-2023之间
o_orderpriority = 1 or o_shippriority = 1
--上面条件筛选率在 5%

```



什么都不优化

执行：时间 1秒多 左右

```sql
explain 
select * from  orders
where o_orderdate > '2022-01-01' 
and ( o_orderpriority = 1 or o_shippriority = 1)
order by o_orderdate desc
limit 20,10;
```

全表扫描

查看每一步的具体执行时间 explain analyze

```sql
explain analyze
select * from  orders
where o_orderdate > '2022-01-01' 
and ( o_orderpriority = 1 or o_shippriority = 1)
order by o_orderdate desc
limit 20,10;
```

```sql
-> Limit/Offset: 10/20 row(s)  (cost=0.35 rows=0) (actual time=0.0203..0.0203 rows=0 loops=1)
     -> Sort: orders.o_orderdate DESC, limit input to 30 row(s) per chunk  (cost=0.35 rows=1) (actual time=0.0195..0.0195 rows=0 loops=1)
         -> Filter: ((or...
```



#### 第一次优化：创建 o_orderdate 降序索引

```sql
create index idx_o_orderdate on orders (orders desc);
```

执行：5ms

很快，基本到这里很好了，但还有优化空间

就是虽然，通过索引可以提高了查询速度，但还是发现，扫描了 19万行

原因 ( o_orderpriority = 1 and o_shippriority = 1) 需要回表进行条件过滤



执行顺序：

```sql
explain analyze
select * from  orders
where o_orderdate > '2022-01-01' 
and ( o_orderpriority = 1 or o_shippriority = 1)
order by o_orderdate desc
limit 20,10


-> Limit/Offset: 10/20 row(s)  (cost=0.71 rows=0) (actual time=0.012..0.012 rows=0 loops=1)
     -> Filter: ((orders.o_shippriority = 1) and (orders.o_orderpriority = 1))  (cost=0.71 rows=1) (actual time=0.0115..0.0115 rows=0 loops=1)
         -> Index ran...
```

explain analyze 后面开始看

首先：Index ran  ，扫描 o_orderdate > '2022-01-01'  所有数据 （19万多条）

然后：条件过滤，(orders.o_shippriority = 1) and (orders.o_orderpriority = 1)

最后：limit 拿到前面30条，抛弃前面20条



 #### 第二次优化

由于 ( o_orderpriority = 1 or o_shippriority = 1) 条件导致需要回表进行条件过滤，导致limit 无法提前过滤

那就将 or  拆出来，不仅可以使用联合索引，也可以提前 limit 减少扫描表

```sql
explain analyze
select * from (
(
    select * from  orders
	where o_orderpriority = '1' and o_orderdate > '2022-01-01' 
 	order by o_orderdate desc 
    limit 30 
)
union
(
    select * from  orders
	where o_shippriority = 1 and o_orderdate > '2022-01-01' 
    order by o_orderdate desc 
    limit 30 )
) t
order by o_orderdate desc
limit 20,10;

create index idx_o_orderdate1 on orders (o_orderpriority,o_orderdate desc);
create index idx_o_orderdate2 on orders (o_shippriority,o_orderdate desc);

```



有坑需要注意：（因为有这样的坑，所以一般第一次优化结束就基本可以了）

1.  union 之间的select需要 （） 包起来，不然 mysql 看到 order by limit 以为sql结束了
2. 使用 union ，不能使用 union  all ,因为可能有重复的
3. limit 需要 30 ，因为你的条件是 limit 20,10;
4. o_orderpriority 是 chat ，不是使用 1 导致隐式转换，不走索引
5. 最后60条 需要文件排序，60条文件排序可以忽略的



优化后：

执行时间 4 ms，和第一次优化差不多

优点：扫描行变少了

总共两次索引扫描，

一次  o_orderpriority = '1' and o_orderdate > '2022-01-01'  ，扫描 1万9，过滤 30条

一次  o_shippriority = 1 and o_orderdate > '2022-01-01'   ，扫描 1万8，过滤 30条

最后 60 条 union  + 文件排序



比较第一次优化：

 o_orderdate > '2022-01-01'  扫描  19万 

然后条件 (orders.o_shippriority = 1) and (orders.o_orderpriority = 1) 

最后拿到 30 条



比较第一次优化，第二次优化在数据越来越多的情况下，适应性更好

毕竟扫描的行更少了



同时缺点也很明显：有坑需要注意，实现起来很麻烦，还容易出错









### 什么情况下mysql索引会失效

- **不规范的使用：使用 <>， != ，Not In ，like已 '%...'开头，对字段表达式操作**

  注意：

  is null 是会走索引的 相当与常量，可以走任何索引

  col = key or col is null ，ref_or_null

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

  







### 面试误区

使用 or 不走索引的，建议使用 union all

这是有误的  or  两边都有索引还是会走索引的，使用的是索引合并（单表有效）



or 两边相同列，有索引，走索引

or 两边不同列，有索引，走索引，还是索引合并



还有 union all 有 两边的并集问题

使用 union  去重性能又达不到





### 两个单列索引，不如一个联合索引

冗余一个联合索引



### 索引合并

在mysql5.0中已经实现了索引合并

多个单列索引在单表里面可以合并的

索引合并的时候，会对索引进行并集，交集或者先交集再并集操作，以便合并成一个索引。

不能对多表进行索引合并



### 函数索引

MySQL 8.0.13 之前不支持函数索引

小写函数：创建索引 -- email 字段小写唯一的约束

处理 DATETIME 类型 ，又函数 date 执行的

处理 JSON 类型



### 降序索引

mysql 8.0 默认升序排序的 asc

避免 Using filesort



### Using filesort 出现情况

只有 order by 才会出现的

1. order by 顺序与索引的顺序不对
2. order by 多个字段的排序没有对应的联合索引（多个单索引是会导致Using filesort）

解决：降序就用降序索引，多个字段的排序要将对应的联合索引



### 覆盖索引 

利用覆盖索引来进行查询操作，避免回表

说明：如果一本书需要知道第 11 章是什么标题，会翻开第 11 章对应的那一页吗？目录浏览一下就好，这

个目录就是起到覆盖索引的作用。













































































