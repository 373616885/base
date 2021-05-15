### 数据库自增ID用完了会怎样

自增ID达到上限用完了之后，分为两种情况：

1. 如果设置了主键，那么将会报错主键冲突。
2. 如果没有设置主键，数据库则会帮我们自动生成一个全局的row_id，新数据会覆盖老数据

解决方案：

表尽可能都要设置主键，主键尽量使用bigint类型，21亿的上限还是有可能达到的，比如魔兽，虽然说row_id上限高达281万亿，但是覆盖数据显然是不可接受的。





mysql 里int类型是4个字节 

有符号位的话就是[-2^31,2^31-1]，也就是2147483647 

无符号位的话最大值就是2^32-1，也就是4294967295

row_id ： 6个字节 2^48-1 ，281474976710655



### 有主键

```sql
CREATE TABLE `test1` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
 `name` varchar(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2147483647 DEFAULT CHARSET=utf8mb4;

插入
insert into test1(name) values('qq');
再次插入
insert into test1(name) values('ww');

mariadb： Out of range value for column 'id' at row 1
mysql:Duplicate entry '2147483647' for key 'test1.PRIMARY'

```



**如果设置了主键并且自增的话，达到自增主键上限就会报错重复的主键key**

**解决方案: mysql主键改为bigint，也就是8个字节**



### 没有主键

```sql
CREATE TABLE `test2` (
 `name` varchar(32) NOT NULL DEFAULT ''
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
```

通过`ps -ef|grep mysql`拿到mysql的进程ID，然后执行命令，通过gdb先把row_id修改为1

```shell
ps -ef|grep mysql

sudo gdb -p 2584 -ex 'p dict_sys->row_id=1' -batch
```

然后插入几条数据

```sql
insert into test2(name) values('1');
insert into test2(name) values('2');
insert into test2(name) values('3');
```

再次修改row_id为2^48，也就是281474976710656

```shell
sudo gdb -p 2584 -ex 'p dict_sys->row_id=281474976710656' -batch
```



**然后查询数据会发现4条数据，分别是4，5，6，3。**

**因为我们先设置row_id=1开始，所以1，2，3的row_id也是1，2，3。**

**修改row_id为上限值之后，row_id会从0重新开始计算，所以4，5，6的row_id就是0，1，2。**

**由于1，2数据已经存在，数据则是会被覆盖。**

