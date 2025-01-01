import { createApp } from 'vue'
import 'element-plus/theme-chalk/index.css' // 引入 Element Plus 样式文件
import 'element-plus/theme-chalk/el-message-box.css'
import 'element-plus/theme-chalk/el-message.css'

import { VueMasonryPlugin } from 'vue-masonry' //瀑布流布局插件

import 'virtual:svg-icons-register'
import SvgIcon from './components/SvgIcon.vue'

import pinia from './stores'

import App from './App.vue'

import { i18n } from '@/i18n'
import { filter } from '@/lib/filter'

import router from './router'
import './router/permission'

// 正式的时候注释掉
import '@/mock'

const app = createApp(App)
app.use(pinia)
app.component('svg-icon', SvgIcon)
app.use(router)
app.use(i18n)
app.mount('#app')
app.config.globalProperties.$filter = filter

// 注册 Vue Masonry 插件
app.use(VueMasonryPlugin)
