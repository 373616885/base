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



### Undo log

**undo log主要用于事务回滚时恢复原来的数据**

mysql在执行sql语句时，会将一条逻辑相反的日志保存到undo log中。因此，undo log中记录的也是逻辑日志。

当sql语句为insert时，会在undo log中记录本次插入的主键id。等事务回滚时，delete此id即可。

当sql语句为update时，会在undo log中记录修改前的数据。等事务回滚时，再执行一次update，得到原来的数据。

当sql语句为delete时，会在undo log中记录删除前的数据。等事务回滚时，insert原来的数据即可。



undo log 日志是可以被删除的，当产生 insert 语句后，事务一旦提交，

undo log 中的 insert 语句就可以被立即删除，因为 undo log 只会在回滚时用到，

因为`insert`操作的记录，只对事务本身可见，对其他事务不可见



像 update、delete 语句则不会立即删除，因为还有可能其他事务再读取这些数据，

有专门的`purge`线程进行删除





### 额外

MySQL的innodb每一行都会有三个默认隐藏字段row_id, tx_id ,roll_point

分别是行id，事务id，roll_point回滚指针，指向的是上一个版本的数据

roll_point：每次修改数据（插入没有）都会在 undo日志中写入老数据，这个roll_point就是存在undo日志中上一个版本数据的地址指针

MySQL8.0自带工具 ibd2sdi 可以查看

![](img\20220914142941238.png)



### **readView 结构**

开启事务时，创建 readView，相当于一个数组

m_ids：当前活跃读写事务

mix_trx_id ：当前活跃读写最小事务ID

max_trx_id ：当前活跃读写最大事务ID

creator_trx_id : 生成该读写事务的ID





### **mvcc** 

开启事务时，创建 readView （一个事务一个readView ）

**在事务中 select 规则：** 

select 的时候如果当前数据的 tx_id 比 mix_trx_id 也就是最小事务ID 小（在readView左边），可以访问，证明当前事务点已提交，

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



### 问题

为什么上了写锁，别的事务还可以读操作？

因为InnoD‍B有**MVCC机制（多版本并发控制）**，可以使用快照读，而不会被阻塞。

select 的时候，与第一次的版本是一致的，称为快照读

select 的时候，通过最新的readView ，读到最新的数据，称为当前读



### 现实中常出现的问题



```java
@Transactional
public void updateCourse(UpdateCourseDto courseDto) {

    // 查询数据
    Course course = examCourseMapper.selectByCourseId(courseDto.getId());

    // 锁数据 select for-update
    examCourseMapper.selectForUpdate(courseDto.getId());

    // 判断数据的状态--再次查询数据（一个事务里的可重复读之前的数据了）
    Boolean isStatus = examCourseMapper.selectStatus(courseDto.getId());

    // 可以插人充值记录
    if (isStatus) {
        examMemoyMapper.insert(new Memoy());
        //修改主表几率
        examCourseMapper.updateStatus(courseDto.getId());
    }
}
```

常见错误：

以为两个线程进来是 select for-update 锁住了，

第一个线程，先往下执行，判断是否可以插人几率

判断完成插人新记录，修改状态，事务提交，第二个线程是否锁

然后往下执行，读取到数据的状态已经变了，就可以控制住



其实是错误的，因为mysql默认是可重复读

在一个事务中，前后两次读取是不会读到，别人提交的数据的，只会与第一次读的数据一致

上面的错误就是

第一层读到的数据和下面的读到的数据 其实是一样的，并不会读到另一个事务提交的数据









### 还有一种情况就是分布式锁与事务不同步

```java
@Transactional
public void updateCourse(UpdateCourseDto courseDto) {

    // 其他业务

    // 分布式锁的力度比事务小，导致别的线程读到的数据还是没有扣减的数据
	boolean isLock = redis.lock("key:"+courseDto.getId())
    if(isLock) {
        try {
            //查询扣点数据
		    Integer num =examCourseMapper.selectNum(courseDto.getId());
            //数据扣减逻辑
            examCourseMapper.deduct(courseDto.getId(),num)
        }
        finally {
            redis.releaseLock("key:"+courseDto.getId())
        }
    }
        
	    
    // 其他业务
    
    .....
    // 提交事务    
        
}
```





### 总结

#### 锁的使用粒度一定要大于事务

































