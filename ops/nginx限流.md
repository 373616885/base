## ngx_http_limit_req_module

限流

- 空桶
- Burst
- NoDelay

Nginx限流使用的是leaky bucket算法



### 空桶

最简单的限流配置开始：

```
limit_req_zone $binary_remote_addr zone=ip_limit:10m rate=10r/s;

server {
    location /login/ {
        limit_req zone=ip_limit;
        proxy_pass http://login_upstream;
    }
}
```

- `$binary_remote_addr` 针对客户端ip限流；
- `zone=ip_limit:10m` 限流规则名称为`ip_limit`，允许使用10MB的内存空间来记录ip对应的限流状态；
- `rate=10r/s` 限流速度为每秒10次请求
- `location /login/` 对登录进行限流



限流速度为每秒10次请求，如果有10次请求同时到达一个空闲的nginx，他们都能得到执行吗？

答案：不能，`rate=10r/s` 每秒10次请求，平均每100ms漏出一个桶，去执行

如果10次请求同时到达，那么只有一个请求能够得到执行，其它的，都会被拒绝

等到下一个100ms才能接收请求

![](img\c5bc911822bbddb9cef1b11944759a8c1.png)





漏桶漏出请求是匀速的。10r/s是怎样匀速的呢？每100ms漏出一个请求。

在这样的配置下，桶是空的，所有不能实时漏出的请求，都会被拒绝掉。

所以如果10次请求同时到达，那么只有一个请求能够得到执行，其它的，都会被拒绝。

这不太友好，大部分业务场景下我们希望这10个请求都能得到执行。





### Burst

把配置改一下，解决上一节的问题

```
limit_req_zone $binary_remote_addr zone=ip_limit:10m rate=10r/s;

server {
    location /login/ {
        limit_req zone=ip_limit burst=12;
        proxy_pass http://login_upstream;
    }
}
```

burst=12 漏桶的大小设置为12

![](img\c5bc911822bbddb9cef1b11944759a8c2.png)

逻辑上叫漏桶，实现起来是FIFO队列，把得不到执行的请求暂时缓存起来。

这样漏出的速度仍然是100ms一个请求，但并发而来，暂时得不到执行的请求，可以先缓存起来。只有当队列满了的时候，才会拒绝接受新请求。

这样漏桶在限流的同时，也起到了削峰填谷的作用。

在这样的配置下，如果有10次请求同时到达，它们会依次执行，每100ms执行1个。

虽然得到执行了，但因为排队执行，延迟大大增加，在很多场景下仍然是不能接受的





### NoDelay

继续修改配置，解决Delay太久导致延迟增加的问题

```
limit_req_zone $binary_remote_addr zone=ip_limit:10m rate=10r/s;

server {
    location /login/ {
        limit_req zone=ip_limit burst=12 nodelay;
        proxy_pass http://login_upstream;
    }
}
```



nodelay 把开始执行请求的时间提前，以前是delay到从桶里漏出来才执行，现在不delay了，只要入桶就开始执行



![](img\c5bc911822bbddb9cef1b119442259a8c2.png)



要么立刻执行，要么被拒绝，请求不会因为限流而增加延迟了。

因为请求从桶里漏出来还是匀速的，桶的空间又是固定的，最终平均下来，还是每秒执行了5次请求，限流的目的还是达到了。

但这样也有缺点，限流是限了，但是限得不那么匀速。以上面的配置举例，如果有12个请求同时到达，那么这12个请求都能够立刻执行，然后后面的请求只能匀速进桶，100ms执行1个。如果有一段时间没有请求，桶空了，那么又可能出现并发的12个请求一起执行。

大部分情况下，这种限流不匀速，不算是大问题。不过nginx也提供了一个参数才控制并发执行也就是nodelay的请求的数量。



```
limit_req_zone $binary_remote_addr zone=ip_limit:10m rate=10r/s;

server {
    location /login/ {
        limit_req zone=ip_limit burst=12 delay=4;
        proxy_pass http://login_upstream;
    }
}
```



delay=4 从桶内第5个请求开始delay

![](img\c5bc911822bbddb9cef1b119222259a8c2.png)

这样通过控制delay参数的值，可以调整允许并发执行的请求的数量，使得请求变的均匀起来，在有些耗资源的服务上控制这个数量，还是有必要的。









## ngx_http_limit_conn_module

限制连接数

- limit_conn_zone
- limit_conn



### limit_conn_zone

```
limit_conn_zone $binary_remote_addr zone=addr:10m;
```

- `limit_conn_zone`只能够在`http`块中使用
- limit_conn_zone：用来配置限流key及存放key对应信息的共享内存区域大小。此处的key是“$binary_remote_addr”，表示IP地址为键。
- limit_conn_status：配置被限流后返回的状态码，默认返回503。
- limit_conn_log_level：配置记录被限流后的日志级别，默认error级别。
- $binary_remote_addr   针对客户端ip限流。也可以使用$server_name作为key来限制域名级别的最大连接数
- zone=addr:10m  限制连接数规则名称为 addr ，允许使用10MB的内存空间来记录ip对应的限制状态；



### limit_conn

```
server {
     location /get/ {
         # 指定每个给定键值的最大同时连接数，同一IP同一时间只允许有5个连接
         limit_conn addr 5;
     }
 }
```

imit_conn：要配置存放key和计数器的共享内存区域和指定key的最大连接数。

此处指定的最大连接数是5，表示Nginx最多同时并发处理5个连接

















