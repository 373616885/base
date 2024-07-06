### count(*) ,count(1)，count(字段) 的区别

count(*) 与 count(1) 在innodb中是没有区别的

count(*)  只要有索引，没有where的条件就会在索引上统计

有 where 条件就会在根据where来统计



例如：T_TableA   有主键 A ,  和 索引 B（没有where条件）

```mysql
EXPLAIN
SELECT count(*) from t_city ;

Using index
```

发现 走 索引 B , 因为在索引上节点存的字段更少

主键 A 节点存的所有字段信息



MySAM 会直接在表中拿（没有where条件）

```mysql
EXPLAIN
SELECT count(*) FROM t_city

Select tables optimized away

```





count(*) 会统计 null 字段

count(字段) 不会统计 null 字段



MySAM 里面 count(*)  在没有where 的前提下，会比较快

因为MySAM 有单独的地方记录表的行数



总结：

1. count(*)  在没有where 的前提下，MySAM会在表信息中直接获取
2. innodb 中count(*) 会选择利用索引统计行数
3. count(*) 与 count(1) 是没有区别的，不管在innodb还是MySAM
4.  count(字段) 没有索引，就会全表扫描，同时过滤掉 null 
5. 有where 条件会根据条件来看是否走索引

















