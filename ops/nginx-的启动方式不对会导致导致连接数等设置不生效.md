## nginx启动方式

```shell
1 /usr/sbin/nginx
2 service nginx start
3 systemctl start nginx
```

2、3两种方式是一样

那么usr/sbin/nginx启动和 service nginx start 有什么区别？



**最大的区别是可能会导致连接数等设置不生效！**



## 问题过程

nginx进程的最大的连接数依赖一下几个设置：

1、首先是系统对进程的连接数控制 ulimit -a 可查看

```shell
[root@web nginx]# ulimit -a
core file size          (blocks, -c) unlimited
data seg size           (kbytes, -d) unlimited
scheduling priority             (-e) 0
file size               (blocks, -f) unlimited
pending signals                 (-i) 14504
max locked memory       (kbytes, -l) 64
max memory size         (kbytes, -m) unlimited
open files                      (-n) 1000000
pipe size            (512 bytes, -p) 8
POSIX message queues     (bytes, -q) 819200
real-time priority              (-r) 0
stack size              (kbytes, -s) 8192
cpu time               (seconds, -t) unlimited
max user processes              (-u) 1000001
virtual memory          (kbytes, -v) unlimited
file locks                      (-x) unlimited
```

看到系统设置为100万 `open files (-n) 1000000`

3、`service nginx start`

4、找到nginx的进程id，查看`cat /proc/pid/limits`

```shell
[root@web init.d]# cat /proc/32630/limits 
Limit                     Soft Limit           Hard Limit           Units     
Max cpu time              unlimited            unlimited            seconds   
Max file size             unlimited            unlimited            bytes     
Max data size             unlimited            unlimited            bytes     
Max stack size            8388608              unlimited            bytes     
Max core file size        0                    unlimited            bytes     
Max resident set          unlimited            unlimited            bytes     
Max processes             14504                14504                processes 
Max open files            1024                 4096                 files     
Max locked memory         65536                65536                bytes     
Max address space         unlimited            unlimited            bytes     
Max file locks            unlimited            unlimited            locks     
Max pending signals       14504                14504                signals   
Max msgqueue size         819200               819200               bytes     
Max nice priority         0                    0                    
Max realtime priority     0                    0                    
Max realtime timeout      unlimited            unlimited            us        

```

发现一个很奇怪的地方，明明系统open file 设置100万，但是进程却只有1024！

改用`/usr/sbin/nginx` 启动，连接数正常

```shell
[root@web nginx]# cat /proc/2861/limits 
Limit                     Soft Limit           Hard Limit           Units     
Max cpu time              unlimited            unlimited            seconds   
Max file size             unlimited            unlimited            bytes     
Max data size             unlimited            unlimited            bytes     
Max stack size            8388608              unlimited            bytes     
Max core file size        unlimited            unlimited            bytes     
Max resident set          unlimited            unlimited            bytes     
Max processes             1000001              1000001              processes 
Max open files            1000000              1000001              files     
Max locked memory         65536                65536                bytes     
Max address space         unlimited            unlimited            bytes     
Max file locks            unlimited            unlimited            locks     
Max pending signals       14504                14504                signals   
Max msgqueue size         819200               819200               bytes     
Max nice priority         0                    0                    
Max realtime priority     0                    0                    
Max realtime timeout      unlimited            unlimited            us      

```



说明使用service nginx start启动，链接数哪里被重新修改限制了。



## 总结

如果未修改最大连接数，使用`service nginx start`启动nginx，

那当请求并发达到1024整个服务会遇到连接数问题。

所以，启动成功后要用`cat /proc/pid/limits`查看连接数是否准确！不然线上很容易出现问题！

