

### Canal的工作原理

1. canal模拟mysql slave与mysql master的交互协议，伪装自己是一个mysql slave，向mysql master发送dump协议；
2. mysql master收到mysql slave（canal）发送的dump请求，开始推送binlog增量日志给slave(也就是canal)；
3. mysql slave（canal伪装的）收到binlog增量日志后,就可以对这部分日志进行解析，获取主库的结构及数据变更；



### SpringBoot整合Canal(同步MySQL到Redis)



1. 引用客户端依赖

  ```xml
  <dependency>
   <groupId>top.javatool</groupId>
   <artifactId>canal-spring-boot-starter</artifactId>
   <version>1.2.1-RELEASE</version>
  </dependency>
  ```

  

2. 配置文件中添加相关配置

  ```yaml
  canal:
    destination: heima # canal的集群名字，要与安装canal时设置的名称一致
    server: 192.144.226.196:11111 # canal服务地址
  ```
  
  
  
3. 在相应的实体类上添加注解

  - canal并不依赖与mybatis，所以数据库表与实体类映射关系还是需要一些注解表明
    - @Id：这个是一定要加的，标注主键
    - @Column：如果列名不一致时，这个需要加
    - @Transient：标记不属于表中的字段

```java
package com.example.item.pojo;
 
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
 
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
 
@Data
@TableName("tb_item")
public class Item {
    @TableId(type = IdType.AUTO)
    @Id
    private Long id;//商品id
    private String name;//商品名称
    private String title;//商品标题
    private Long price;//价格（分）
    private String image;//商品图片
    private String category;//分类名称
    private String brand;//品牌名称
    private String spec;//规格
    private Integer status;//商品状态 1-正常，2-下架
    private Date createTime;//创建时间
    private Date updateTime;//更新时间
    @TableField(exist = false)
    @Transient
    private Integer stock;
    @Transient
    @TableField(exist = false)
    private Integer sold;
}
```



4. 编写监听器组件，监听canal事件

```java
package com.example.item.canal;
 
import com.example.item.pojo.Item;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;
 
@CanalTable("tb_item")
@Component
public class ItemHandler implements EntryHandler<Item> {
 
 
    @Override
    public void insert(Item item) {
        // 写数据到JVM进程缓存
        System.out.println("写数据到jvm进程缓存的逻辑，这里省略了");
        // 写数据到redis
        System.out.println("写数据到redis缓存的逻辑，这里省略了");
    }
 
    @Override
    public void update(Item before, Item after) {
        // 写数据到JVM进程缓存
        System.out.println("更新数据到jvm进程缓存的逻辑，这里省略了");
        // 写数据到redis
        System.out.println("更新数据到redis缓存的逻辑，这里省略了");
    }
 
    @Override
    public void delete(Item item) {
        // 删除数据到JVM进程缓存
        System.out.println("删除数据到jvm进程缓存的逻辑，这里省略了");
        // 删除数据到redis
        System.out.println("删除数据到redis缓存的逻辑，这里省略了");
    }
 
}
```

- @CanalTable：标识要处理哪张表（毕竟canal没有依赖mybatis）
- @Component：将组件交给容器管理 

>  至此，整合已经完毕