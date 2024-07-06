import { createApp } from 'vue'
import App from './App.vue'
// 全局过滤器
import { filters } from '@/utils/filters.js'

const app = createApp(App)
app.config.globalProperties.$filters = filters
console.log(filters.sexName(1))
app.provide('sexList', [
  { name: '未知', value: 0 },
  { name: '男', value: 1 },
  { name: '女', value: 2 }
])
app.mount('#app')
