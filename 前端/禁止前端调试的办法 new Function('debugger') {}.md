### 禁止前端调试的技巧

一个简单的方法是写定时器放置debugger

在页面上加入

```js
<script>
    window.setInterval(() => {
    new Function('debugger')()
}, 1000);
</script>
```





另一个方法是使用库禁用DevTools，检测开启则跳转首页，关闭则跳转原页，增加破解门槛。使用简单，只需引入一个JS



禁用DevTools工具叫：

**disable-devtool**

**GitHub地址：**https://github.com//theajack/disable-devtool



```js
npm i disable-devtool
```



main.js中定义相关的配置，其实这里配置在官方文档中有很多，这里不做过多赘述，有需求可以去看官方文档

```js
import DisableDevtool from 'disable-devtool';

DisableDevtool();

或者
DisableDevtool({
  url: 'about:blank',
  timeOutUrl: 'about:blank',
  disableMenu: true
});

```



