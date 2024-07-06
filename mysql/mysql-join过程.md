### mysql join 过程

mysql 的 join 是拿到两张表的数据 M * N （迪尔卡积）的形式去比较



mysql优化的地方

加入 join_buffer



每次 Table1 中的 M条数据放到 join_buffer 里面

然后用 join_buffer  里的数据 去 和Table2  的数据 比较



只需要读一次（扫描一次 Table2 表）就可以比较完一个 join_buffer 

不需要 Table1  每一行，都去读一遍 Table2 



![](img\20220914142941251.png)



尽管 join_buffer 优化但还很耗性能



多表 join 造成影响：

1. join 导致大量数据（多张表数据）进入内存中，导致别的热点数据被淘汰，影响其他业务
2. 迪尔卡积的形式比较 增加 系统负载（增加了 CPU 开销）
3. join 查询跨业务，两个不相干的表，业务放在一起--后续进行拆表拆库很难 --（商品和用户关联在一起，就很难拆）
4. join 不好优化，例如：后续优化，商品放到 es 里面，join 根本无法通过缓存优化





这也是为什么开发规范禁止超过三张表 使用 join 的根本原因，在设计阶段就必须尽量减少JOIN操作

![](img\20220914142941266.png)



解决办法：

join 的字段使用索引，那么他们就不需要比较，直接索引定位

可以整 hash 索引，hash join，hash直接定位（只能等值连接）

建议拆分多次单表操作



注意点：

Join Buffer（连接缓冲区）是优化器用于处理连接查询操作时的临时缓冲区

每个join有一个单独的缓冲区（只有一个）





### 为什么需要小表驱动大表

join过程：

Block nested-loop join（BNL算法）先将 驱动表数据加载到join buffer里面（小表优先）拿到的 M 条数据

然后扫描被驱动表 N 比较

注意：如果驱动表（小表）过大，join buffer无法一次性装载驱动表的结果集，将会分阶段与被驱动表进行批量数据匹配，

会增加被驱动表的扫描次数，从而降低查询效率。所以开发中要遵守小表驱动大表的原则。





mysql 默认是会优化的



在JOIN查询中经常用到的 inner join、left join、right join



问题解答：

1. 当使用left join时，左表是驱动表，右表是被驱动表 ;

2. 当使用right join时，右表时驱动表，左表是被驱动表 ;

3. 当使用inner join时，mysql会选择数据量比较小的表作为驱动表，大表作为被驱动表 ;



为什么？

小表驱动大表 > A驱动表，B被驱动

```
 for(200条){
   for(20万条){
     ...
   }
 }

```

大表驱动小表 > B驱动表，A被驱动表

```
for(20万){
   for(200条){
    ...
   }
 }
```



- 如果小的循环在外层，对于表连接来说就只连接200次 ;（扫描被驱动表200次数）
- 如果大的循环在外层，则需要进行20万次表连接，从而浪费资源，增加消耗 ;（扫描被驱动表20万次数）









### 具体过程

##### 创建student表

```sql
DROP TABLE IF EXISTS student;
CREATE TABLE student (
  id int(10) NOT NULL AUTO_INCREMENT COMMENT '序号',
  student_id INT NOT NULL COMMENT '学号',
  name varchar(20) COMMENT '姓名',
  department varchar(200) COMMENT '院系',
  remarks varchar(400) COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

##### 创建scores表

```sql
DROP TABLE IF EXISTS scores;
CREATE TABLE scores (
   id INT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '序号',
   student_id INT NOT NULL COMMENT '学号',
   course_name VARCHAR(50) NOT NULL COMMENT '课程名称',
   score INT NOT NULL COMMENT '分数',
   remarks varchar(400) COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

##### 添加索引

```sql
ALTER TABLE student ADD index idx_name_department (name, department);
```

##### 插入数据

```sql

INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('刘零',1,'美术','备注0');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('郑一',2,'土木','备注1');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('吴二',3,'数学','备注2');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('张三',4,'中文','备注3');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('李四',5,'英语','备注4');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('王五',6,'美术','备注5');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('钱六',7,'土木','备注6');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('孙七',8,'数学','备注7');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('赵八',9,'英语','备注8');
INSERT INTO `student` (`name`,`student_id`,`department`,`remarks`) values ('周九',10,'数学','备注9');
```

```sql

BEGIN
  DECLARE v_name VARCHAR(20);
  DECLARE v_department VARCHAR(200);
  DECLARE i INT DEFAULT 0;
  DECLARE n INT DEFAULT 100000;
  DECLARE v_max_id INT DEFAULT 1;
  set autocommit = 0;
  select max(id) into v_max_id from student;
  REPEAT
    set i = i + 1;
    set v_max_id = v_max_id + 1;
    set v_name = CONCAT('mock_name',i);
    set v_department = CONCAT('mock_department',i);
    INSERT INTO `student` (`student_id`,`name`,`department`,`remarks`) values (v_max_id,v_name,v_department,'mock_remark');

    INSERT INTO `scores` (`student_id`,`course_name`,`score`,`remarks`) values (v_max_id,CONCAT('mock_Chinese',i),RAND()*(100-50)+50,'mock_remarks');
    INSERT INTO `scores` (`student_id`,`course_name`,`score`,`remarks`) values (v_max_id,CONCAT('mock_Math',i),RAND()*(100-50)+50,'mock_remark');
    INSERT INTO `scores` (`student_id`,`course_name`,`score`,`remarks`) values (v_max_id,CONCAT('mock_English',i),RAND()*(100-50)+50,'mock_remarks');

    UNTIL i = n
  END REPEAT;
  COMMIT;
  set autocommit = 1;
END
```





```sql
EXPLAIN
select * from scores left join student on student.id = scores.student_id;
```



```sql
EXPLAIN
select * from student left join scores on student.id = scores.student_id;
```

##### 分阶段匹配过程如下

1. 先把student表前15条数据读到join buffer中。

2. 然后用scores表去匹配join buffer中的前15条。

3. 记录下匹配结果。

4. 清空join buffer。

5. 再把student表后15条读取join buffer中。

6. 然后用scores表去匹配join buffer中的后15条。

7. 记录下匹配结果。



![](img\20220914142941267..jpg)





