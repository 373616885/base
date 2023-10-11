import { createApp } from 'vue'
import App from './App.vue'
import Foot from './page/Foot.vue'
const app = createApp(App)
// 只能在 createApp 与 mount('#app')中间注册
/**
 *  全局注册 :
 *  缺点：
 *      全局注册的组件，既是没有用到也会被打包
 *      依赖不明确，根全局变量一样
 */
app.component('Foot', Foot)
app.mount('#app')
