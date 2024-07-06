### 全局API

https://cn.vuejs.org/api/application.html#application-api



#### `app.component`

全局注册组件

#### `app.config` 

app.config.globalProperties.x = 99

报错不认 x  https://cn.vuejs.org/guide/typescript/options-api.html#augmenting-global-properties

 ```ts
  declare module 'vue' {
    interface ComponentCustomProperties {
      x: number
    }
  }
 ```

  

#### `app.directive`

自定义指令

```vue
app.directive('beaty', (el: any, value: any) => {
  // 这会在 `mounted` 和 `updated` 时都调用
  el.innerText += value
  el.style.color = 'green'
  el.style.backgroundColor = 'yellow'
})
<!-- 使用 -->
<h4 v-beaty='msg'>directive</h4>
```



#### `app.mount`

挂载在一个容器元素中

#### `app.unmount`

卸载整个APP

```ts
// 5S后卸载整个app
setTimeout(() => {
  app.unmount()
}, 5000)
```



#### `app.use`

