### MVCC

多版本并发控制，读取数据时，通过一种类似快照的方式将数据保存下来

不同的事务session 看到的 特定版本的数据

这样读锁和写锁就不冲突了



### 作用范围

MVCC 只对 Read Commited （读已提交） 和 Repatable Read（可重复读）

这两个事务隔离级别下生效

其他两个事务隔离级别不兼容：

Read UnCommited （读未提交）总是读最新的数据，不需要事务版本控制

Serializable （串行化）：有事务的时候对行数据加锁，select 需要等待 



### 额外

MySQL的innodb每一行都会有三个默认隐藏字段row_id, tx_id ,roll_point

分别是行id，事务id，roll_point回滚指针，指向的是上一个版本的数据

roll_point：每次修改数据（插入没有）都会在 undo日志中写入老数据，这个roll_point就是存在undo日志中上一个版本数据的地址指针

![](img\20220914142941238.png)



### **readView 结构**

开启事务时，创建 readView，相当于一个数组

m_ids：当前活跃读写事务

mix_trx_id ：当前活跃读写最小事务ID

max_trx_id ：当前活跃读写最大事务ID

creator_trx_id : 生成该读写事务的ID





**mvcc** 

开启事务时，创建 readView （一个事务一个readView ）

**在事务中 select 规则：** 

select 的时候如果当前数据的 tx_id 比 mix_trx_id 也就是最小事务ID 小（在readView左边），可以访问，证明当前事务已提交，

（直接读数据表的行数据）



select 的时候如果当前数据的 tx_id 比 max_trx_id 最大事务ID 都大或者在readView范围（在readView右边或者范围），证明当前事务没提交，不可以访问

接着在 roll_point 找到上一条数据的 tx_id ，重新比较看是否在readView右边或者范围，在，则接着取上一个版本的 tx_id 

直到 在readView左边，取当前undo里面的数据







**读提交：**

事务A 中读到，事务B已提交的数据

每次 select 的时候就会产生一个新的readView ，读到最新的数据，称为当前读



**可重复读：**

事务A 中读不到事务B提交的数据

mvcc  开启事务时，创建 readView，之后不会更新readView

select 的时候，与第一次的版本是一致的，称为快照读



这就是Mysql 的MVCC，通过 tx_id  版本链，实现多版本并发读写

和不同的readView生成策略，实现不同的事务隔离



### mysql 默认可重复读

就是为了节省性能不需要每次都创建新的 readView

