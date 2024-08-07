import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import 'element-plus/theme-chalk/index.css' // 引入 Element Plus 样式文件
import 'element-plus/theme-chalk/el-message-box.css'
import 'element-plus/theme-chalk/el-message.css'

import 'virtual:svg-icons-register'
import SvgIcon from './components/SvgIcon.vue'

import App from './App.vue'

import { i18n } from '@/i18n'

// 正式的时候注释掉
//import '@/mock'

import './router/permission'

const app = createApp(App)
app.use(createPinia())
app.component('svg-icon', SvgIcon)
app.use(router)
app.use(i18n)
app.mount('#app')
