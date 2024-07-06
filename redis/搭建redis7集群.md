### 配置文件修改

cp redis.conf  redis-7001.conf

进行如下内容修改：

port 7001 

daemonize yes 开启守护进程

cluster-enabled yes  开启集群

cluster-config-file nodes-7001.conf  集群节点配置文件区分

appendonly yes  开启AOF



#### 外网主机访问额外配置

关掉bind：#bind 127.0.0.1 -::1  因需要外面主机访问所以不能绑定

或者 bind 0.0.0.0 -::1

protected-mode no  关闭保护模式（官方的关闭保护模式后果自负）

redis 访问密码

requirepass 123456

主要是针对master对应的slave节点设置的，在slave节点数据同步的时候用到。设置集群节点间访问密码

masterauth 123456

```shell
##########redis集群需要至少三个master节点，我们这里搭建三个master节点，并且给每个master再搭建一个slave节点，总共6个redis节点

bind 0.0.0.0 -::1
#是否开启保护模式，默认开启。要是配置里没有指定bind和密码。开启该参数后，redis只会本地进行访问，拒绝外部访问。
protected-mode no
port 7001
# redis进程是否以守护进程的方式运行，yes为是，no为否(不以守护进程的方式运行会占用一个终端)。
daemonize yes

# 指定redis进程的PID文件存放位置
pidfile "/var/run/redis_7001.pid"

# redis日志级别，可用的级别有debug.verbose.notice.warning
loglevel notice

# log文件输出位置，如果进程以守护进程的方式运行，此处又将输出文件设置为stdout的话，就会将日志信息输出到/dev/null
logfile "/usr/local/redis/logs/redis-7001.log"

# 指定存储至本地数据库时是否压缩文件，默认为yes即启用存储
rdbcompression yes

# # 指定本地数据库文件名
dbfilename "dump-7001.rdb"

#redis集群模式数据存放目录，指定数据文件存放位置，必须要指定不同的目录位置，不然会丢失数据
dir "/usr/local/redis/7001/data/"

################################# Redis集群配置 [无中心化]#################################
# 开启redis集群
cluster-enabled yes

# 集群配置文件
cluster-config-file nodes-7001.conf

# 设定节点失联时间，超过该时间（毫秒），集群自动进行主从切换。默认15秒
cluster-node-timeout 15000

# redis访问密码
requirepass 123456


# 主要是针对master对应的slave节点设置的，在slave节点数据同步的时候用到。设置集群节点间访问密码
masterauth 123456

#AOF模式 
appendonly yes

# 混合持久化模式
aof-use-rdb-preamble yes

# maxclients 10000

```





redis_7001.conf配置文件修改完成， redis_7002.conf 只需要复制7001，然  后把7001替换为7002即可

cp redis_7001.conf redis_7002.conf

vi  redis_7002.conf  直接输入:%s/7001/7002/g  然后保存文件即可

接着：redis_7003.conf



如果要是三主三从，换个机子再搭三个从的

要加上密码或者内网不要密码也行



### 启动redis

```shell
redis-server redis-7001.conf
redis-server redis-7002.conf
redis-server redis-7003.conf
```



进入7001节点核查

```shell
redis-cli -p 7001

redis-cli -a 密码 -h 127.0.0.1 -p 7001
```

开始cluster_state:fail

![](img\2020023104.webp)



### 启动三台主机的集群模式

```shell
redis-cli --cluster create --cluster-replicas 0 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003

有密码
redis-cli -a 密码 --cluster create --cluster-replicas 0 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003

```

```shell
--cluster-replicas 0 只有主没有从
--cluster-replicas 1 表示每个master有1个slave
--cluster-replicas 3 表示每个master有3个slave
三主三从 127.0.0.1 三台
redis-cli --cluster create --cluster-replicas 1 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 127.0.0.1:7006
```





cluster info（查看集群信息）、cluster nodes（查看节点列表）

```shell
E:\docker\Redis-7.0.8-Windows-x64>redis-cli -p 7001
127.0.0.1:7001> cluster info
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:3
cluster_size:3
cluster_current_epoch:3
cluster_my_epoch:1
cluster_stats_messages_ping_sent:1292
cluster_stats_messages_pong_sent:1260
cluster_stats_messages_sent:2552
cluster_stats_messages_ping_received:1258
cluster_stats_messages_pong_received:1292
cluster_stats_messages_meet_received:2
cluster_stats_messages_received:2552
total_cluster_links_buffer_limit_exceeded:0
127.0.0.1:7001>
```



### **检查集群信息**

**redis-cli --cluster check 127.0.0.1:7001**



至此，redis集群搭建成功



### 正常关闭集群服务器

```shell
[root@ca01 redis-cluster]# cat stopall.sh 
cd 7001
./redis-cli -p 7001 shutdown
cd ..
cd 7002
./redis-cli -p 7002 shutdown
cd ..
cd 7003
./redis-cli -p 7003 shutdown
cd ..
cd 7004
./redis-cli -p 7004 shutdown
cd ..
cd 7005
./redis-cli -p 7005 shutdown
cd ..
cd 7006
./redis-cli -p 7006 shutdown
cd ..
[root@ca01 redis-cluster]#
```



### 重新启动集群

```shell
[root@ca01 redis-cluster]# cat startall.sh 
cd 7001
./redis-server redis-7001.conf
cd ..
cd 7002
./redis-server redis-7002.conf
cd ..
cd 7003
./redis-server redis-7003.conf
cd ..
cd 7004
./redis-server redis-7004.conf
cd ..
cd 7005
./redis-server redis-7005.conf
cd ..
cd 7006
./redis-server redis-7006.conf
cd ..
[root@ca01 redis-cluster]# 
```



### Spring boot 整合

pom.xml

```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
	<!-- 我们在配置文件显式地配置Redis客户端时，需要添加commons-pool2连接池依赖 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-pool2</artifactId>
    </dependency>
```

yaml

```yaml
#yaml配置
spring:
  data:
    redis:
      database: 0
      cluster:
        nodes: 127.0.0.1:7001,127.0.0.1:7002,127.0.0.1:7003,127.0.0.1:7004,127.0.0.1:7005,127.0.0.1:7006
      #password: xxxx
      #当我们在配置文件显式地配置Redis客户端时，需要添加commons-pool2连接池依赖
      lettuce:
        pool:
          enabled: true
          max-active: 10  #连接池最大连接数
          max-idle: 8     #连接池中最大空闲连接数
          max-wait: -1ms  #连接池最大等待阻塞时间
          min-idle: 0     #连接池中最小空闲数


```

RedisConfig

```java
package com.qin.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {
    // Key 过期时间: 1day = 86400s
    private Duration timeToLive = Duration.ofDays(1);

    // Spring Cache 配置类
    @Bean(name="cacheManager")
    public CacheManager cacheManager(RedisConnectionFactory factory){
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl( timeToLive )         // 设置缓存的过期时间
                .computePrefixWith(cacheName -> cacheName + ":")    // 无该行代码，则Spring Cache 默认使用::用作命名空间的分隔符
                .serializeKeysWith( RedisSerializationContext.SerializationPair.fromSerializer( getKeySerializer() ) )  // 设置Key序列化器
                .serializeValuesWith( RedisSerializationContext.SerializationPair.fromSerializer( getValueSerializer() ) ) // 设置Value序列化器
                .disableCachingNullValues();

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults( redisCacheConfiguration )
                .build();
        log.info(" 自定义Spring Cache Manager配置完成 ... ");
        return redisCacheManager;
    }

    // Redis 配置类
    // 自定义的RedisTemplate的Bean名称必须为 redisTemplate。当方法名不为 redisTemplate时，可通过name显示指定bean名称，@Bean(name="redisTemplate")
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 设置String Key序列化器
        template.setKeySerializer( getKeySerializer() );
        template.setValueSerializer( getValueSerializer() );
        // 设置Hash Key序列化器
        template.setHashKeySerializer( getKeySerializer() );
        template.setHashValueSerializer( getValueSerializer() );
        log.info("自定义RedisTemplate配置完成 ... ");
        return template;
    }

    // key 采用String序列化器
    private RedisSerializer<String> getKeySerializer() {
        return new StringRedisSerializer();
    }

    // value 采用Json序列化器
    private RedisSerializer<Object> getValueSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

}

```

使用

```
@ResponseBody
@RestController
@AllArgsConstructor
public class Test {

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("name")
    public String name() {
        return redisTemplate.opsForValue().get("name");
    }
}
```