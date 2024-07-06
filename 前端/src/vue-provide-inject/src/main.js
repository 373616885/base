import { createApp } from 'vue'
import App from './App.vue'

/**
 * import App from './App.vue'
 * App：根组件，所有组件都从它开始，都是他的子组件
 *
 *  app : vue的实例对象
 *  一个项目中，有且只有一个vue实例对象
 *
 *
 *  挂载应用
 *  app.mount('#app')
 *  应用必须调用.mount() 方法之后才会渲染出来
 *  该方法需要一个接收 ‘ 容器 ’ 的参数，可以是一个时间DOM或者CSS选择器
 *  <div id="app"></div>
 *
 *
 *  浏览器可执行文件
 *  1.html
 *  2.css
 *  3.js
 *  4.image
 *
 *  .vue 浏览器是不支持的
 *  通过打包工具 ：webpack  vite
 *  打包成 main.js ，vue项目的入口
 *
 *
 *  */
const app = createApp(App)

app.provide('golabData', '全局数据: qinjp')

app.mount('#app')
