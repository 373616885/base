### grafana+prometheus + Mysqld-Exporter 搭建MySql监控系统实践



### 安装Mysqld-Exporter

下载镜像

```shell
docker pull bitnami/mysqld-exporter
```



创建容器并启动

```shell
docker run -d -p 9104:9104 -e DATA_SOURCE_NAME="root:password@(127.0.0.1:3306)/qin_db"  --name mysqld-exporter bitnami/mysqld-exporter:latest
```



验证

http://127.0.0.1:9104/



### 安装prometheus 

下载镜像

```shell
docker pull bitnami/prometheus:latest
```

安装启动

```shell
docker run -d -p 9090:9090 --name prometheus -v /path/to/prometheus-persistence:/opt/bitnami/prometheus/data -v E:/docker/mysql/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml bitnami/prometheus:latest
    
docker run -d -p 9090:9090 --name prometheus -v E:/docker/mysql/prometheus/data:/opt/bitnami/prometheus/data -v E:/docker/mysql/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml bitnami/prometheus:latest
```

验证

http://localhost:9090/



prometheus.yml

```
cp /etc/prometheus/prometheus.yml /opt/bitnami/prometheus/data/
cp /opt/bitnami/prometheus/data/prometheus.yml   /etc/prometheus/prometheus.yml


prometheus.yml
添加：
  - job_name: "mysql"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["127.0.01:9104"]

```





### 安装 grafana

下载镜像

```powershell
 docker pull grafana/grafana
```

安装启动

```shell
docker run -d -p 3000:3000 --name=grafana grafana/grafana
```

验证

http://127.0.0.1:3000/login 

初始登录参数：admin/admin





### 网络问题

```shell
docker network create mynetwork --driver bridge
```



```shell
docker run -d -p 3306:3306 --net=mynetwork --network-alias mysql --name mysql -e MYSQL_ROOT_PASSWORD=373616885 -e TZ=Asia/Shanghai mysql:8.0.33 


docker run -d -p 9104:9104 --net=mynetwork --network-alias mysqld-exporter -e DATA_SOURCE_NAME="root:373616885@(192.168.1.101:3306)/" --name mysqld-exporter bitnami/mysqld-exporter:latest

ip：为宿主IP
查看mysqld-exporter的日志：是否正常，如果不正常会导致grafana没有数据的

docker run -d -p 9090:9090 --net=mynetwork --network-alias prometheus --name prometheus -v E:/docker/mysql/prometheus/data:/opt/bitnami/prometheus/data -v E:/docker/mysql/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml bitnami/prometheus:latest

prometheus.yml 中的ip 为宿主的IP


docker run -d -p 3000:3000 --net=mynetwork --network-alias grafana --name=grafana grafana/grafana

```









### prometheus

http://localhost:9090/

注意添加：

```json
prometheus.yml
添加：
  - job_name: "mysql"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["宿主IP:9104"]
```



查看添加的 targets 是否正常

![](img\20220914142941252.png)

![](img\20220914142941253.png)





### grafana



#### 添加prometheus数据源

Home --> Connections --> Data sources -->Add new data source

![](img\20220914142941254.png)



选择 Prometheus

![](img\20220914142941255.png)



设置prometheus URL地址并Save&Test：

添加：HTTP url 

http://192.168.1.101:9090

出现这个表示成功

![](img\20220914142941257.png)



#### 导入MySQL监控模板

找到 Home --> dashboard  --> new -->import

![](img\20220914142941258.png)



填入模板链接：https://grafana.com/grafana/dashboards/7362-mysql-overview

![](img\20220914142941259.png)

选择 prometheus 

![](img\20220914142941260.png)





### 导入后，即可查看对应Dashboard

发现没有数据：

![](img\20220914142941261.png)

查看mysqld_exporter的日志发现： root @ 密码错误

docker logs mysqld-exporter

```verilog
ts=2023-06-05T08:00:09.384Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:00:24.384Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:00:39.384Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:00:54.401Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:01:09.390Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:01:24.388Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:01:39.387Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:01:54.383Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"
ts=2023-06-05T08:02:09.383Z caller=exporter.go:149 level=error msg="Error pinging mysqld" err="Error 1045: Access denied for user 'root'@'172.18.0.1' (using password: YES)"

```

解决之后

![](img\20220914142941262.png)