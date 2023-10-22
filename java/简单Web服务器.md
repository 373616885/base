### Java 18 新特性：简单Web服务器 jwebserver

- 构建目的是应用于测试与教学
- 不提供身份验证、访问控制或加密等安全功能
- 仅支持HTTP/1.1，不支持HTTPS
- 仅支持GET、HEAD请求
- 可以通过命令行、Java类启动



一个html文件

```html
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>基本框架</title>
    <script src="./vue.js"></script>
</head>

<body>
    <div id="app">
        <h2>{{msg}}</h2>
        <button type="button" @click="changeMsg">点击</button>
        <input v-model="msg" />
    </div>
    <script type="text/javascript">
        let app = new Vue({
            el: '#app',
            data() {
                return {
                    msg: 'Hello Vue.js!'
                }
            },
            methods: {
                changeMsg() {
                    this.msg = "qinjp";
                }
            }
        });
    </script>
</body>

</html>
```



```shell
jwebserver -p 9000 -d / -b 127.0.0.1 -o info

D:\Java\openjdk-21\jdk-21\bin\jwebserver.exe -p 9000 -d D:\GitHub\base\前端\src\vue-cdn -b 127.0.0.1 -o info

```

- `-b`：要绑定的ip地址
- `-p`：要启动的访问端口
- `-d`：要提供服务的目录
- `-o`：控制台的输出级别





### java6  httpserver API

```java
package com.qin.blog.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author qinjp
 * @since 2023-10-22
 */
public class HttpServerUtils {

    public static void main(String[] args) throws IOException {
        //创建HttpServer服务器
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 10);
        //将 / 请求交给Handler处理器处理
        httpServer.createContext("/", new HttpServerHandler());
        httpServer.start();
    }


}

class HttpServerHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        String url = httpExchange.getRequestURI().toString();

        //请求头
        Headers headers = httpExchange.getRequestHeaders();

        byte[] bytes = httpExchange.getRequestBody().readAllBytes();

        Set<Map.Entry<String, List<String>>> entries = headers.entrySet();

        StringBuilder response = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : entries) {
            response.append(entry.toString())
                    .append("\n");
        }

        String requestBody = new String(bytes);

        response.append(requestBody);



        System.out.println("url:" + url);
        System.out.println("headers:");
        System.out.println(entries);
        System.out.println("requestBody:");
        System.out.println(requestBody);

        //设置响应头属性及响应信息的长度
        httpExchange.sendResponseHeaders(200, response.length());
        //获得输出流
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();

    }



}
```



