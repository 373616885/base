### 面试题

一张表里面有 7 条数据，id （主键）从1-7，删除了最后两条数据，重启mysql数据库，又插入一条数据，此时 id 是几？



条件 ：mysql 5.7 innodb

自增 主键 ID 是存储在内存中的

重启了服务器内存中的id 就会被清除

最后由于删除了两条，id为6和7 ，存在磁盘中的id最大值是5 

所以新插入的id 为 6

特例：如果配置了自增ID走磁盘，那就是8



更近一步出现的问题：

重启了，与 id 关联的外键业务表数据没有删除，会导致业务数据不一致

解决办法：

1. 雪花ID

2. 分布式ID

   



MyISAM

自增ID在磁盘中，每次都从磁盘中读取

所以和Innodb不同新插入的id是 8



更近一步

MyISAM不适合写多的情况

只适合读多的情况，



数据量：408434条，141.7M，50个并发,查询1000次,结果如下:

Mysql8使用InnoDB引擎测试

![](img\20200520093455479.png)

Mysql8使用MyISAM引擎测试

![](img\20200520093555404.png)

由上面结果可知，MySQL8中的MyISAM引擎的查询效率近乎是InnoDB引擎的50倍



MySQL5.7使用MyISAM引擎测试

![](img\20200520093721698.png)我们可以得到，在MySQL8之后对于MyISAM引擎的一些功能有所舍弃，查询效率有所下降，将InnoDB作为默认的查询引擎








MyISAM 与Innodb 差异
 1. 事务处理上方面
MyISAM 强调的是性能，查询的速度比 InnoDB 类型更快，但是不提供事务支持。InnoDB 提供事务支持事务。

2. 外键
   MyISAM 不支持外键，InnoDB 支持外键。
3. 锁
    MyISAM 只支持表级锁，InnoDB 支持行级锁和表级锁，默认是行级锁，行锁大幅度提高了多用户并发操作的性能。innodb 比较适合于插入和更新操作比较多的情况，而 myisam 则适合用于频繁查询的情况。另外，InnoDB 表的行锁也不是绝对的，如果在执行一个 SQL 语句时，MySQL 不能确定要扫描的范围，InnoDB 表同样会锁全表：
    例如 update table set num=1 where name like “%aaa%”。
 4. 全文索引
      MyISAM 支持全文索引， InnoDB 不支持全文索引。innodb 从 mysql5.6 版本开始提供对全文索引的支持。
 5. 表主键
      MyISAM：允许没有主键的表存在。
      InnoDB：如果没有设定主键，就会自动生成一个 6 字节的主键(用户不可见)。

6. 表的具体行数
   MyISAM：select count() from table,MyISAM 只要简单的读出保存好的行数。因为MyISAM 内置了一个计数器，count()时它直接从计数器中读。

   InnoDB：不保存表的具体行数，也就是说，执行 select count(*) from table 时，InnoDB要扫描一遍整个表来计算有多少行。













