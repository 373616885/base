### 事务四大特性

分别是原子性、一致性、隔离性、持久性。

1.  原子性（Atomicity）：事务中所有操作是不可再分割的原子单位。事务中所有操作要么全部执行成功，要么全部执行失败。
2. 一致性（Consistency）：事务无论成功，还是失败，都必须保持事务内一致。当失败时，前面的操作都要进行回滚，不管中途的状态
3. 隔离性（Isolation）：隔离性是指在并发操作中，不同事务之间应该隔离开来，使每个并发中的事务不会相互干扰
4. 持久性（Durability）：一旦事务提交成功，事务中所有的数据操作都必须被持久化到数据库中，即使提交事务后，数据库马上崩溃，在数据库重启时，也必须能保证通过某种机制恢复数据



### 脏页

所有数据都在内存中，操作的，这时会出现内存与磁盘的数据不一致

脏页：当内存数据页跟磁盘数据页内容不一致的时候，我们称这个内存页为“脏页”

干净页：内存数据页写入磁盘后, 两边内容一致, 此时称为"干净页".



### 刷脏页

InnoDB 在处理更新语句的时候，只做了写日志这一个磁盘操作。

这个日志叫作 **redo log（重做日志）**，在 **更新内存** 并 **写完 redo log** 后，就返回给客户端，本次更新成功。

redo log 数据少，物理上也是连续的，快

刷脏页：把内存脏页写入磁盘（这个过程的术语就是 **flush**）

**一旦持久化到磁盘，redo log中对应的那部分数据就可以释放**



触发数据库flush

1. redo log写满，系统会停止一切更新操作；

2. mysql认为空闲的时候，主动去刷新；
3. 系统内存不足。当需要新的内存页，而内存不够用的时候，就要淘汰一些数据页，空出内存给别的数据页使用
4. MySQL正常关闭的情况。这时候，MySQL会把内存的脏页都flush到磁盘上



### 如何刷新呢

刷新的规则叫checkpoint机制。

- sharp checkpoint：在数据库关闭时，刷新所有的脏页到磁盘，这里有参数控制，默认是开启的
- fuzzy checkpoint：刷新一部分脏页到磁盘中。



Fuzzy checkpoint 

- 定时刷新
  - 以每秒或每十秒的速度从缓冲池的脏页列表中刷新一定比例的页回磁盘
- 当LRU中列表中空闲页不足时，强制LRU删除一些末尾的页
  - 使用innodb_lru_scan_depth来控制最少空闲页的数量
- 当重做日志不够用时，从flush 列表中选择一些页，强制checkpoint刷新
  - 重做日志有两个水位：async水位 75%   innodb的总大小；sync水位：90% innodb大小
  - 当未刷新的数据大小 小于 低水位，不需要刷新
  - 当未刷新的数据大小 大于 低水位，小于高水位，异步刷新，保证刷新后小于 低水位
  - 当未刷新的数据大小 大于 高水位，同步阻塞刷新，保证刷新后小于 低水位。
- 系统中的整体脏页比例，如果达到一定比例，强制刷新
  - 使用 innodb_max_dirty_pages_pct来控制这个比例数值，默认时75%



参数

- innodb_io_capacity
- innodb_max_dirty_pages_pct  默认75%
- innodb_flush_neighbors 刷脏页时，是否归集“邻居”脏页。





### MySQL事务的实现原理

原子性：通过回滚日志（Undo Log）来实现

​	当事务执行时，MySQL 将所有操作的结果记录到（Undo Log）回滚日志中

​	如果事务执行失败，MySQL 将通过回滚日志将所有操作回滚到执行前的状态

一致性：通过使用锁来实现

​	对需要修改的数据行加锁，以防止其他事务对数据的修改导致不一致

隔离性：

​	多个事务并发执行时，每个事务看到的数据应该与其他事务隔离开来

​	通过使用锁和 MVCC（多版本并发控制）来实现

持久性：

​	持久性是指一旦事务提交，对数据所做的修改就应该永久保存在数据库中

​	通过使用重做日志（Redo Log）来实现



MySQL 事务的实现原理涉及到回滚日志、锁、MVCC 和重做日志

通过这些机制来保证事务的原子性、一致性、隔离性和持久性。



### 脏读

在一次事务中，读到了其他事务未提交的事务

### 不可重复读

在一次事务中，如果其他事务对数据进行操作并提交了，导致多次查询结果不一致，

### 幻读

在一个事务中，进行多次范围查询，如果其他事务插入一条数据 ，导致前后两次查询不一致得到的数据个数不一致



脏读 ，不可重复读 都针对一条数据

幻读针对一批数据 （范围查询）--解决：间隙锁





### 事务的隔离级别五种

1. **NONE**
2. **读未提交（Read Uncommitted）**
3. **读提交（Read Committed）**
4. **可重复读（Repeatable Read）**
5. **串行化（Serializable）**



读未提交 Read Uncommitted : 允许脏读

读取到了其他事务未提交的数据



读提交（Read Committed）: 解决脏读--但会出现不可重复读

在同一事务中，别的事务提交了对数据的操作，导致前后查询不一致（不可重复读）



可重复读（Repeatable Read）：解决不可重复读，但会出现幻读

一个事务在前后两次查询同一个范围的时候（> < 这些查询），如果其他事务插入一条数据

前后两次查询不一致（幻读）



### MVCC

MySQL的每一行都会有三个默认隐藏字段row_id, tx_id ,roll_point

分别是行id，事务id，roll_point回滚指针，指向的是上一个版本的数据

roll_point：每次修改数据（插入没有）都会在 undo日志中写入老数据，这个roll_point就是存在undo日志中上一个版本数据的地址指针

![](img\20220914142941238.png)



**readView 结构**

开启事务时，创建 readView

m_ids：当前活跃读写事务

mix_trx_id ：当前活跃读写最小事务ID

max_trx_id ：当前活跃读写最大事务ID

creator_trx_id : 生成该读写事务的ID







**mvcc** 

开启事务时，创建 readView 



select 的时候如果当前数据的 tx_id 比 mix_trx_id 也就是最小事务ID 小（在readView左边），可以访问，证明当前事务已提交，

直接读数据表的行数据



select 的时候如果当前数据的 tx_id 比 max_trx_id 最大事务ID 都大或者在readView范围（在readView右边或者范围），证明当前事务没提交，不可以访问

接着在 roll_point 找到上一条数据的 tx_id ，重新比较看是否在readView右边或者范围，在，则接着取上一个版本的 tx_id 

直到 在readView左边，取当前undo里面的数据







**读提交：**

事务A 中读到，事务B已提交的数据

每次 select 的时候就会产生一个新的readView ，读到最新的数据，称为当前读



**可重复读：**

事务A 中读不到事务B提交的数据

mvcc  开启事务时，创建 readView，之后不会更新readView

select 的时候，与第一次的版本是一致的，称为快照



**这就是Mysql 的MVCC，通过 tx_id  版本链，实现多版本并发读写**

**和不同的readView生成策略，实现不同的事务隔离**



### mysql 默认可重复读

就是为了节省性能不需要每次都创建新的 readView



### 问题

为什么上了写锁，别的事务还可以读操作？

因为InnoD‍B有**MVCC机制（多版本并发控制）**，可以使用快照读，而不会被阻塞。

select 的时候，与第一次的版本是一致的，称为快照读

select 的时候，通过最新的readView ，读到最新的数据，称为当前读