## MySQL 时间字段如何选择

mysql 可以支持的时间类型：

DATETIME、 TIMESTAMP、DATE、TIME、YEAR

几种类型比较如下：

| 日期时间类型 | 占用空间 | 日期格式            | 最小值              | 最大值              | 零值表示            |
| ------------ | -------- | ------------------- | ------------------- | ------------------- | ------------------- |
| DATETIME     | 8 bytes  | YYYY-MM-DD HH:MM:SS | 1000-01-01 00:00:00 | 9999-12-31 23:59:59 | 0000-00-00 00:00:00 |
| TIMESTAMP    | 4 bytes  | YYYY-MM-DD HH:MM:SS | 19700101080001      | 2038 年的某个时刻   | 00000000000000      |
| DATE         | 4 bytes  | YYYY-MM-DD          | 1000-01-01          | 9999-12-31          | 0000-00-00          |
| TIME         | 3 bytes  | HH:MM:SS            | -838:59:59          | 838:59:59           | 00:00:00            |
| YEAR         | 1 bytes  | YYYY                | 1901                | 2155                | 0000                |

 DATE、TIME、YEAR 这些不谈论，看业务实际需要来使用





## 主要谈论datetime、timestamp 和 bigint的选择使用

https://stackoverflow.com/questions/409286/should-i-use-the-datetime-or-timestamp-data-type-in-mysql/20718367#20718367

区别：

- **timestamp会跟随设置的时区变化而变化，而datetime保存的是绝对值不会变化**

一个`timestamp`字段，一个`datetime`字段

修改时区`SET TIME_ZONE = "america/new_york";`后，`timestamp`字段的值变了!

- **占用存储空间不同。timestamp储存占用4个字节，datetime储存占用8个字节**

http://dev.mysql.com/doc/refman/5.7/en/storage-requirements.html

- **可表示的时间范围不同**

timestamp表示范围:`1970-01-01 00:00:00`~`2038-01-09 03:14:07`

datetime支持的范围更宽`1000-01-01 00:00:00` ~ `9999-12-31 23:59:59

- **索引速度不同。timestamp更轻量，索引相对datetime更快**

  

区别用表格汇总如下：

| 区别项     | timestamp                               | datetime                                  |
| ---------- | --------------------------------------- | ----------------------------------------- |
| 和时区有关 | 有                                      | 无                                        |
| 占用字节   | 4字节                                   | 8个字节                                   |
| 时间范围   | 1970-01-01 00:00:00~2038-01-09 03:14:07 | 1000-01-01 00:00:00 ~ 9999-12-31 23:59:59 |
| 索引       | 相对快                                  | 相对慢                                    |



## 测试

## 前期数据准备

通过程序往数据库插入50w数据

- 数据表：

```sql
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `time_date` datetime NOT NULL,
  `time_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `time_long` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `time_long` (`time_long`),
  KEY `time_timestamp` (`time_timestamp`),
  KEY `time_date` (`time_date`)
) ENGINE=InnoDB AUTO_INCREMENT=500003 DEFAULT CHARSET=latin1
```

其中time_long、time_timestamp、time_date为同一时间的不同存储格式

- 实体类users

```java
/**
 * @author hetiantian
 * @date 2018/10/21
 * */
@Builder
@Data
public class Users {
    /**
     * 自增唯一id
     * */
    private Long id;

    /**
     * date类型的时间
     * */
    private Date timeDate;

    /**
     * timestamp类型的时间
     * */
    private Timestamp timeTimestamp;

    /**
     * long类型的时间
     * */
    private long timeLong;
}
```

- dao层接口

```java
/**
 * @author hetiantian
 * @date 2018/10/21
 * */
@Mapper
public interface UsersMapper {
    @Insert("insert into users(time_date, time_timestamp, time_long) value(#{timeDate}, #{timeTimestamp}, #{timeLong})")
    @Options(useGeneratedKeys = true,keyProperty = "id",keyColumn = "id")
    int saveUsers(Users users);
}
```

- 测试类往数据库插入数据

```java
public class UsersMapperTest extends BaseTest {
    @Resource
    private UsersMapper usersMapper;

    @Test
    public void test() {
        for (int i = 0; i < 500000; i++) {
            long time = System.currentTimeMillis();
            usersMapper.saveUsers(Users.builder().timeDate(new Date(time)).timeLong(time).timeTimestamp(new Timestamp(time)).build());
        }
    }
}
```

## sql查询速率测试

- 通过datetime类型查询：

```sql
select count(*) from users where time_date >="2018-10-21 23:32:44" and time_date <="2018-10-21 23:41:22"
```

耗时：0.171

- 通过timestamp类型查询

```sql
select count(*) from users where time_timestamp >= "2018-10-21 23:32:44" and time_timestamp <="2018-10-21 23:41:22"
```

耗时：0.351

- 通过bigint类型查询

```sql
select count(*) from users where time_long >=1540135964091 and time_long <=1540136482372
```

耗时：0.130s

- 结论 在InnoDB存储引擎下，通过时间范围查找，性能bigint > datetime > timestamp



## sql分组速率测试

使用bigint 进行分组会每条数据进行一个分组，如果将bigint做一个转化在去分组就没有比较的意义了，转化也是需要时间的

- 通过datetime类型分组：

```sql
select time_date, count(*) from users group by time_date
```

耗时：0.176s

- 通过timestamp类型分组：

```sql
select time_timestamp, count(*) from users group by time_timestamp
```

耗时：0.173s

- 结论 在InnoDB存储引擎下，通过时间分组，性能timestamp > datetime，但是相差不大

## sql排序速率测试

- 通过datetime类型排序：

```sql
select * from users order by time_date
```

耗时：1.038s

- 通过timestamp类型排序

```sql
select * from users order by time_timestamp
```

耗时：0.933s

- 通过bigint类型排序

```sql
select * from users order by time_long
```

耗时：0.775s

- 结论 在InnoDB存储引擎下，通过时间排序，性能bigint > timestamp > datetime

## 小结

如果需要对时间字段进行操作(如通过时间范围查找或者排序等)，推荐使用bigint，如果时间字段不需要进行任何操作，推荐使用timestamp，使用4个字节保存比较节省空间，但是只能记录到2038年记录的时间有限。

来源：juejin.im/post/6844903701094596615



## 特殊

long与datetime类型相互转换

```sql
#时间datetime转换为long格式
SELECT UNIX_TIMESTAMP(NOW());  #1410403824

#时间long转换为datetime格式
SELECT FROM_UNIXTIME(1410403824);  #2014-09-11 10:50:24


在实际的处理long转换为datetime格式过程中，需要除以1000
一般long = 毫秒的
SELECT FROM_UNIXTIME(create_time/1000) AS createTime,TYPE FROM `mobile_code` ORDER BY id DESC; 



```





