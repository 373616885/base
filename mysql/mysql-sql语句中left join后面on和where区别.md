**在使用left jion时，on和where条件的区别如下：**

1. **on条件是在生成临时表时使用的条件**，它不管on中的条件是否为真，都会返回左边表中的记录。**（这需要特别注意）**

如果左边表的某条记录不符合连接条件，那么它不进行连接，但是仍然留在结果集中（此时右边部分的连接结果为NULL）

2. **where条件是在临时表生成好后，再对临时表进行过滤的条件**。这时已经没有left join的含义（必须返回左边表的记录）了，条件不为真的就全部过滤掉。
3. **建议尽量用where来过滤条件**



```sql
CREATE TABLE `a` (

`id` int(11) NOT NULL AUTO_INCREMENT,

`sid` int(11) NOT NULL,

`type` char(10) NOT NULL,

PRIMARY KEY (`id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `a` (`id`, `sid`, `type`) VALUES (1, 1, 'a');

INSERT INTO `a` (`id`, `sid`, `type`) VALUES (2, 1, 'b');

INSERT INTO `a` (`id`, `sid`, `type`) VALUES (3, 2, 'c');

INSERT INTO `a` (`id`, `sid`, `type`) VALUES (4, 3, 'd');

```

```sql
select * from a left join b on a.sid=b.sid ;
```

| id   | sid  | type | sid(1) | remark |
| ---- | ---- | ---- | ------ | ------ |
| 1    | 1    | a    | 1      | A      |
| 2    | 1    | b    | 1      | A      |
| 3    | 2    | c    | 2      | A      |
| 4    | 3    | d    | 3      | C      |

```sql
select * from a left join b on a.sid=b.sid and b.remark ='A';
```

**左边表数据都显示，右边不符合条件的为NULL**

| id   | sid  | type | sid(1) | remark |
| ---- | ---- | ---- | ------ | ------ |
| 1    | 1    | a    | 1      | A      |
| 2    | 1    | b    | 1      | A      |
| 3    | 2    | c    | 2      | A      |
| 4    | 3    | d    | null   | null   |

```sql
select * from a left join b on a.sid=b.sid where b.remark ='A';
```

**where后的条件是生成临时表后对临时表过滤**

| id   | sid  | type | sid(1) | remark |
| ---- | ---- | ---- | ------ | ------ |
| 1    | 1    | a    | 1      | A      |
| 2    | 1    | b    | 1      | A      |
| 3    | 2    | c    | 2      | A      |









