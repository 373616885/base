### mysql 存储引擎的区别

MyISAM ：

不支持事务，不支持外键，只支持表锁，写的并发度不高

索引和数据分开：3个文件 MYD(放数据)、.FRM（表结构）、MYI（放索引））--非聚集索引

索引叶节点存储的是 ，数据的物理地址

MyISAM不符合ACID，数据可能会丢失（在内存小的机制没办法）内存80M 无法启动innodb



InnoDB：

支持事务（默认的事务隔离级别为可重复读（REPEATABLE-READ），通过MVCC（并发版本控制）来实现）

使用的锁粒度默认为行级锁，可以支持更高的并发

支持外键约束

可以通过自动增长列，方法是auto_increment

存在着缓冲管理，各种缓冲池，将索引和数据全部缓存起来，加快查询的速度；

主键索引和数据：frm （表结构)，idb （主键索引和数据在一个文件里）--聚集索引

主键索引叶节点存储了主键索引和数据









### MySql的锁

锁的种类：表锁，行锁，页锁

表锁：锁表的，MyISAM默认的锁

行锁：锁记录的，innodb默认的锁

页锁：两者之间



表锁种类：读锁，写锁

行锁的种类：读锁，写锁，意向读锁，意向写锁



读锁排斥写锁，不排斥读锁，写锁排斥所有

意向锁是事务里面作用在表中的，主要解决表锁和行锁冲突的问题

假如要加表锁，需要读取所有记录是否加了行锁，现在在表上加了意向锁，就可以不用扫描了





行锁的实现算法有：

记录锁（锁单行记录的）

间隙锁，锁where范围的，

混合锁：两种锁的混合使用，innodb的默认算法，当数据精确到行，就使用记录锁，不是就使用间隙锁

特殊的间隙锁：插入意向间隙锁，只有的insert的时候有，间隙锁默认情况下，在没有锁的情况：插入的是不会有冲突的，间隙锁锁住了，造成性能问题，所以就有个插入间隙锁，防止修改，但可以插入



间隙锁：

可重复读隔离级别的话，才会在间隙上加上间隙锁

update 和 delete 才有间隙锁，insert是插入意向锁

可以解决Phantom Problem（幻读）



| id     | 5    | 6    | 7    | 8    | 9    | 10   | 11   |
| ------ | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| number | 5    | 6    | 6    | 6    | 6    | 6    | 23   |

间隙范围：

update 或者 delete where number=6

间隙范围就是 id (5,11]  左开右闭









**锁的作用是在索引上的：**

**主键索引**

where 里面是主键，就在主键索引上加上锁

**二级索引加锁**

先在二级索引上加锁，回表的时候，再次在主键索引上加锁

二级索引和主键索引上都加一把锁





### 锁优化建议

1. 尽可能让所有数据检索都通过索引来完成，避免无索引行锁升级为表锁

2. 合理设计索引，尽量缩小锁的范围

3. 尽可能较少检索条件，避免间隙锁

4. 尽量控制事务大小，减少锁定资源量和时间长度

5. 尽可能低级别事务隔离



### 事务隔离级别

读未提交，读已提交，可重复读，串性化

读未提交：会产生脏数据

读已提交:   会出现不可重复读，一个事务里面读到相同的数据，两次读取不一致（数据被修改）

可重复读：会出现幻读，一个事务里读一批数据，两次不一致（数据被insert，或者update,delete）

串性化：解决所有问题



### 会出现幻读解决方案

可重复读事务隔离级别里：（只有可重复读才有间隙锁）

会出现幻读，mysql使用间隙锁要保证，不让其出现幻读



### mvcc版本控制

解决：

1. 实现不同的事务隔离级别

2. 解决其读锁和写锁冲突的问题

原理：不同的事务session 看到的 特定版本的数据，



生效 ： Read Commited （读已提交） 和 Repatable Read（可重复读）

事务隔离级别的控制：使用mvcc版本控制，不同的事务session 看到的 不同的read_view



每一行数据都有row_id （记录的唯一值） ,tx_id （事务id）和undo_point（上一个版本的数据--undo.log里面）



每次事务都会产生一个 read_view，上面有当前活跃事务的ID的间值（mixtx_id和max_trx_id ）



在事务中 select 的时候 与 记录上的tx_id 对吧，比前活跃事务的mixtx_id 都小，证明当前的事务点已经提交

直接读取表的行数据



在事务中 select 的时候 与 记录上的tx_id 对吧，比前活跃事务的mixtx_id 大，那么证明当前事务没提交，去undo.log里找上一个版本的数据



**读已提交：**

每次事务读都产生一个新的read_view，通过这个最新的read_view都是读到最新数据，称为当前读



**可重复读：**

事务A 中读不到事务B提交的数据

mvcc  开启事务时，创建 readView，之后不会更新readView

select 的时候，与第一次的版本是一致的，称为快照读



这就是Mysql 的MVCC，通过 tx_id  版本链，实现多版本并发读写

通过不同的readView生成策略，实现不同的事务隔离





### 为什么上了写锁，别的事务还可以读操作？

因为InnoD‍B有**MVCC机制（多版本并发控制）**，可以使用快照读，而不会被阻塞。

select 的时候，与第一次的版本是一致的，称为快照读

select 的时候，通过最新的readView ，读到最新的数据，称为当前读



### 索引合并

在mysql5.0中已经实现了索引合并

多个单列索引在单表里面可以合并的

索引合并的时候，会对索引进行并集，交集或者先交集再并集操作，以便合并成一个索引。

不能对多表进行索引合并

所有一条联合索引要比多个单列索引性能要好，尽量能使用联合索引就用联合索引



### 一个条sql可以使用多少条索引

建议5个以内，一个表最多16个索引

多个单列索引可能会被合并



### 连接参数

当前连接数（Threads_connected）

最大连接数（max_connections）

一个重点监控的mysql的指标



### innodb内存参数

buffer pool 参数优化

大的内存，可以减小 I/O 磁盘次数，让操作尽量多的在内存中

建议在专门的数据库服务器上，可以设置物理内存的 60% - 80% 

默认128M 

show variables like 'innodb_buffer_pool_size%';



```mysql
mysql -uroot -p

mysql> show variables like 'innodb_buffer_pool_size%';
+-------------------------+-----------+
| Variable_name           | Value     |
+-------------------------+-----------+
| innodb_buffer_pool_size | 134217728 |
+-------------------------+-----------+
1 row in set (0.00 sec)

mysql> select 134217728/1024/1024;
+---------------------+
| 134217728/1024/1024 |
+---------------------+
|        128.00000000 |
+---------------------+
1 row in set (0.00 sec)

mysql>
```



内存调整时机：



内存缓冲池的命中率低于 90 % ，去填加内存



show status like 'innodb_buffer_pool_reads'; 

表示内存中无法满足要求，需要从磁盘中读取



show status like 'innodb_buffer_pool_read_requests';

表示内存中读取页的请求次数



命中率: 

innodb_buffer_pool_reads  /   (innodb_buffer_pool_reads + innodb_buffer_pool_read_requests)    * 100



show status like 'innodb_buffer_pool_read%'; 

```mysql
mysql> show status like 'innodb_buffer_pool_read%';
+---------------------------------------+-------+
| Variable_name                         | Value |
+---------------------------------------+-------+
| Innodb_buffer_pool_read_ahead_rnd     | 0     |
| Innodb_buffer_pool_read_ahead         | 0     |
| Innodb_buffer_pool_read_ahead_evicted | 0     |
| Innodb_buffer_pool_read_requests      | 24202 |
| Innodb_buffer_pool_reads              | 924   |
+---------------------------------------+-------+
5 rows in set (0.00 sec)

mysql> select 24202/(24202 + 924) * 100;
+---------------------------+
| 24202/(24202 + 924) * 100 |
+---------------------------+
|                   96.3225 |
+---------------------------+
1 row in set (0.00 sec)
```



Page 页管理

show status like 'innodb_buffer_pool_pages%'; 

```mysql
mysql> show status like 'innodb_buffer_pool_pages%';
+----------------------------------+-------+
| Variable_name                    | Value |
+----------------------------------+-------+
| Innodb_buffer_pool_pages_data    | 1066  |
| Innodb_buffer_pool_pages_dirty   | 0     |
| Innodb_buffer_pool_pages_flushed | 205   |
| Innodb_buffer_pool_pages_free    | 7119  |
| Innodb_buffer_pool_pages_misc    | 7     |
| Innodb_buffer_pool_pages_total   | 8192  |
+----------------------------------+-------+
6 rows in set (0.00 sec)

mysql> show variables like "innodb_page_size";
+------------------+-------+
| Variable_name    | Value |
+------------------+-------+
| innodb_page_size | 16384 |
+------------------+-------+
1 row in set (0.00 sec)

```

Innodb_buffer_pool_pages_data :   正在使用Page 页个数

Innodb_buffer_pool_pages_dirty：脏页个数

Innodb_buffer_pool_pages_flushed：刷脏个数

Innodb_buffer_pool_pages_free：剩余Page 页个数

Innodb_buffer_pool_pages_total：Page 总个数大小



page size 是 16KiB，通过 show variables like "innodb_page_size" 拿到。









### 什么时候不要使用索引

1. 经常删除改的列
2. 大量重复的列，辨识度不高
3. 表记录很少的（千条，万条以内）