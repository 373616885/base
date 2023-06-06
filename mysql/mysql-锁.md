### 锁的类型

MySQL大致可归纳为以下3种锁

- 表级锁：开销小，加锁快；不会出现死锁；锁定粒度大，发生锁冲突的概率最高，并发度最低。
- 行级锁：开销大，加锁慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低，并发度也最高。
- 页面锁：开销和加锁时间界于表锁和行锁之间；会出现死锁；锁定粒度界于表锁和行锁之间，并发度一般





### **表级锁**

ＭySQL的表锁有两种模式：

读锁（read lock），也叫共享锁（shared lock）：针对同一份数据，多个读操作可以同时进行而不会互相影响（select）。但会阻塞对同一表的写请求

写锁（write lock），也叫排他锁（exclusive lock）：当前操作没完成之前，会阻塞其它读和写操作（update、insert、delete）



MyISAM 存储引擎默认锁



特点

1. 对整张表加锁

2. 开销小

3. 加锁快

4. 无死锁

5. 锁粒度大，发生锁冲突概率大，并发性低

结论

1. 读锁会阻塞写操作，不会阻塞读操作

2. 写锁会阻塞读和写操作

建议

MyISAM的读写锁调度是写优先，已获得读锁的也要在请求队列里等到

这也是MyISAM不适合做写为主表的引擎，因为写锁以后，其它线程不能做任何操作，大量的更新使查询很难得到锁，从而造成永远阻塞。



**如何上锁？**

隐式上锁（默认，自动加锁自动释放）

```sqlite
select //上读锁
insert、update、delete //上写锁
```

显式上锁（手动）

```sql
lock table tableName read;//读锁
lock table tableName write;//写锁
```

解锁（手动）

```sql
unlock tables;//所有锁表
```



```sql
lock table teacher read;// 上读锁
select * from teacher; // session01可以正常读取  
select * from teacher;// session02可以正常读取
update teacher set name = 3 where id =2;//session01报错，因被上读锁不能写操作  update teacher set name = 3 where id =2;// session02被阻塞
unlock tables;// 解锁
update teacher set name = 3 where id =2;// 更新操作成功
 
 
lock table teacher write;// 上写锁
select * from teacher; // session01可以正常读取  
select * from teacher;// session02被阻塞
update teacher set name = 3 where id =2;// session01可以正常更新操作  
update teacher set name = 4 where id =2;// session02被阻塞
unlock tables;// 解锁
select * from teacher;// 读取成功
update teacher set name = 4 where id =2;// 更新操作成功
```



### **行锁**

种类

读锁（read lock），也叫共享锁（shared lock）

允许一个事务去读一行，阻止其他事务获得相同数据集的排他锁

写锁（write lock），也叫排他锁（exclusive lock）

允许获得排他锁的事务更新数据，阻止其他事务取得相同数据集的共享锁和排他锁

意向共享锁（IS）

一个事务给一个数据行加共享锁时，必须先获得表的IS锁

意向排它锁（IX）

一个事务给一个数据行加排他锁时，必须先获得该表的IX锁



InnoDB 存储引擎默认锁



特点

1. 对一行数据加锁
2. 开销大
3. 加锁慢
4. 会出现死锁
5. 锁粒度小，发生锁冲突概率最低，并发性高



事务并发带来的问题

1. 更新丢失

解决：让事务变成串行操作，而不是并发的操作，即对每个事务开始---对读取记录加排他锁

2. 脏读

解决：隔离级别为Read uncommitted

3. 不可重读

解决：使用Next-Key Lock算法来避免

4. 幻读

解决：间隙锁（Gap Lock）



**如何上锁？**

隐式上锁（默认，自动加锁自动释放）

```sql
select //不会上锁
insert、update、delete //上写锁
```

显式上锁（手动）

```sql
select * from tableName lock in share mode;//读锁
select * from tableName for update;//写锁
```

解锁（手动）

*1. 提交事务（commit）*

*2. 回滚事务（rollback）*

*3. kill 阻塞进程*

```sql
begin;
select * from teacher where id = 2 lock in share mode;// session01上读锁
select * from teacher where id = 2;// session02可以正常读取
update teacher set name = 3 where id =2;// session01可以更新操作  
update teacher set name = 5 where id =2;// session02被阻塞
commit;
update teacher set name = 5 where id =2;// 更新操作成功
```

```sql
begin;
select * from teacher where id = 2 for update;// session01上写锁
select * from teacher where id = 2;// session02可以正常读取
update teacher set name = 3 where id =2;// session01可以更新操作
update teacher set name = 5 where id =2;// session02被阻塞
rollback;
update teacher set name = 5 where id =2;// 更新操作成功
```



### 问题

为什么上了写锁，别的事务还可以读操作？

因为InnoD‍B有**MVCC机制（多版本并发控制）**，可以使用快照读，而不会被阻塞。





### **行锁的实现算法**

明白：行锁是作用在主键索引上的，没有定义主键索引，也会默认给一个rowid

**Record Lock 锁** （单个行记录上的锁）





### **页锁**

开销、加锁时间和锁粒度介于表锁和行锁之间，会出现死锁，并发处理能力一般



















