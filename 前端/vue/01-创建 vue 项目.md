### 创建 vue 项目

点击查看[官方文档](https://cli.vuejs.org/zh/guide/creating-a-project.html#vue-create)

> 备注：目前`vue-cli`已处于维护模式，官方推荐基于 `Vite` 创建项目。

官方文档：https://vuejs.org/guide/quick-start.html#using-vue-from-cdn

中文官方文档：https://cn.vuejs.org/guide/quick-start.html#using-vue-from-cdn

```shell
npm init vue@latest

或者

npm create vue@latest
```



### 构建一个 Vite + Vue 项目

官方文档：https://cn.vitejs.dev/guide/

```shell
npm create vite@latest my-vue-app -- --template vue

样例：
npm create vite@latest blog-app -- --template vue
```



### 使用CDN

缺点：

在每个页面都 new Vue() 一个实例，非常的麻烦，并且组件无法复用

```html
  <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>

  <div id="app">{{ message }}</div>

  <script>
    const { createApp } = Vue
    createApp({
      data() {
        return {
          message: 'Hello Vue!'
        }
      }
    }).mount('#app')
  </script>
```



### petite-vue

简化版：只有7kb，上面的有400多kb

```html
 <div id="app">
    <h3>{{message}}</h3>
    <button type="button" @click="changeMsg">点击</button>
    <input v-model="message" />
  </div>

  <script type="module">
    import { createApp } from './petite-vue.es.js'
    createApp({
      message: 'Hello Vue!',
      changeMsg() {
        this.message = 'qinjp'
      }
    }).mount('#app')
  </script>
```



### 直接引入Vue.js实现简单地开发

```html
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>基本框架</title>
    <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
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

