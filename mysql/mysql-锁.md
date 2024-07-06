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

表锁不会出现死锁





### **如何上锁？**

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



### **表锁分析**

```
show status like 'table%';
```

table_locks_waited

出现表级锁定争用而发生等待的次数（不能立即获取锁的次数，每等待一次值加1），此值高说明存在着较严重的表级锁争用情况

table_locks_immediate

产生表级锁定次数，不是可以立即获取锁的查询次数，每立即获取锁加1





### **行锁**

#### **种类**

**读锁（read lock）**，也叫共享锁（shared lock）

允许一个事务去读一行，阻止其他事务获得相同数据集的排他锁

**写锁（write lock）**，也叫排他锁（exclusive lock）

允许获得排他锁的事务更新数据，阻止其他事务取得相同数据集的共享锁和排他锁

**意向共享锁（IS）**

一个事务给一个数据行加共享锁时，必须先获得表的IS锁，阻止其他事务

**意向排它锁（IX）**

一个事务给一个数据行加排他锁时，必须先获得该表的IX锁

**意向锁：**

表锁和行锁虽然锁定范围不同，但是会相互冲突。

当你要加表锁时，势必要先遍历该表的所有记录，判断是否有排他锁。

这种遍历检查的方式显然是一种低效的方式，MySQL引入了意向锁，来检测表锁和行锁的冲突

当事务要在记录上加上行锁时，要首先在表上加上意向锁。这样判断表中是否有记录正在加锁就很简单了，只要看下表上是否有意向锁就行了，从而就能提高效率。





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



### **如何上锁？**

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

select 的时候，与第一次的版本是一致的，称为快照读

select 的时候，通过最新的readView ，读到最新的数据，称为当前读





### **行锁的实现的三种算法**

注意：行锁是作用在索引上的，没有定义索引，也会默认给一个rowid

**总结：使用了 where ，才会使用行锁，作用在索引上的（主键索引，普通索引，隐式索引）**



### **注意点**

1. 只有通过索引条件检索数据时，InnoDB才会使用行级锁，否则会使用表级锁(索引失效，行锁变表锁)

2. 即使是访问不同行的记录，如果使用的是相同的索引键，会发生锁冲突

3. 如果数据表建有多个索引时，可以通过不同的索引锁定不同的行



### **三种算法**

1. record lock：记录锁，单个行记录的锁
2. Gap Lock：间隙锁,锁定一个范围,但不包含记录本身，遵循左开右闭原则。
3. Next_key Lock：Gap Lock+ Record Lock,锁定一个范围,并且锁定记录本身。InnoDB默认加锁方式是Next_key Lock



### 锁的种类

#### **Record Lock 锁 （单个行记录上的锁）**

单个行记录上的锁

Record Lock总是会去锁住索引记录，

如果InnoDB存储引擎表建立的时候没有设置任何一个索引

这时InnoDB存储引擎会使用隐式的主键来进行锁定



#### **Gap Lock 锁（where范围数据）**

当我们用范围条件而不是相等条件检索数据，并请求共享或排他锁时

InnoDB会给符合条件的已有数据记录的索引加锁，对于键值在条件范围内但并不存在的记录，也会被无辜的锁定

可以解决Phantom Problem（幻读）

可重复读隔离级别的话，才会在间隙上加上间隙锁

update 和 delete 才有间隙锁，insert是插入意向锁

**间隙的范围**

![](img\20220914142941264.png)

where number=6;

那么间隙锁锁定的间隙为：（5，11）空闲块

所以你再想插入，更新，删除5到11之间的数就会被阻塞。



**间隙锁的作用**

阻止多个事务将纪录插入到同一个范围内，保证某个间隙内的数据在锁定情况下不会发生任何变化。可以解决Phantom Problem（幻读）

用户可以通过以下两种方式来显式地关闭Gap Lock:

1. 将事务的隔离级别设置为 READ COMMITTED
2. 将参数 innodb_locks_unsafe_for_binlog设置为1



最后需再次提醒的是,对于唯一键值的锁定, Next-Key Lock降级为Record Lock仅存在于查询所有的唯一索引列。

若唯一索引由多个列组成,而查询仅是查找多个唯一索引列中的其中一个,

那么查询其实是range类型查询,而不是 point类型查询,

故InnoDB存储引擎依然使用 Next-Key Lock进行锁定。



#### **Next-key Lock 锁**

同时锁住数据+间隙锁（临键锁）

在Repeatable Read隔离级别下，Next-key Lock 算法是默认的行记录锁定算法。

对于唯一键值的锁定, Next-Key Lock降级为Record Lock仅存在于查询所有的唯一索引列。



#### 插入意向锁（Insert Intention Lock）

插入意图锁是一种间隙锁，在行执行 INSERT 之前的插入操作设置

例子：

有值为4和7的索引记录

一个事务插入值为 5 的记录

一个事务插入值为 6 的记录

两个事务都使用插入意向锁锁住 4 和 7 之间的间隙，

但两者之间并不会相互阻塞，因为这两行并不冲突。



插入意向锁只会和 间隙或者 Next-key 锁冲突，不影响其他事务加其他任何锁

例如：一个事务已经获取了插入意向锁，对其他事务是没有任何影响的；



间隙锁作用就是防止其他事务插入记录造成幻读针对update和delete

插入意向锁针对 insert的





### 行锁分析

```
show status like 'innodb_row_lock%';
```

1. innodb_row_lock_current_waits //当前正在等待锁定的数量

2. innodb_row_lock_time //从系统启动到现在锁定总时间长度

3. innodb_row_lock_time_avg //每次等待所花平均时间

4. innodb_row_lock_time_max //从系统启动到现在等待最长的一次所花时间

5. innodb_row_lock_waits //系统启动后到现在总共等待的次数

**information_schema 库**

1. innodb_lock_waits表

2. innodb_locks表

3. innodb_trx表





### **InnoDB**对于单行数据的加锁原理

**主键索引加锁**

```sql
update user set age = 10 where id = 49;
```

第一条SQL使用主键查询，只需要在 id = 49 这个主键索引上加上锁。



**二级索引加锁**

```sql
update user set age = 10 where name = 'Tom';
```

首先在 name = Tom 这个索引上加写锁，

然后由于使用 InnoDB 二级索引还需再次根据主键索引查询

最后在 id = 49 这个主键索引上加锁。

使用二级索引需要在二级索引和主键索引上各加一把锁。

![](img\20220914142941263.png)



**涉及多个行加锁**

```sql
update user set age = 10 where id > 49;
```

MySQL Server 会根据 WHERE 条件读取第一条满足条件的记录

然后 InnoDB 引擎会将第一条记录返回并加锁

接着 MySQL Server 发起更新改行记录的 UPDATE 请求，更新这条记录

一条记录操作完成，再读取下一条记录，直至没有匹配的记录为止







### 优化建议

1. 尽可能让所有数据检索都通过索引来完成，避免无索引行锁升级为表锁

2. 合理设计索引，尽量缩小锁的范围

3. 尽可能较少检索条件，避免间隙锁

4. 尽量控制事务大小，减少锁定资源量和时间长度

5. 尽可能低级别事务隔离











### **页锁**

开销、加锁时间和锁粒度介于表锁和行锁之间，会出现死锁，并发处理能力一般





### 悲观锁 和 乐观锁

锁实现的一种机制

悲观锁：每次拿数据的时候都会上锁，像表锁，行锁，写锁等

乐观锁：MVCC的实现









