import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
const app = createApp(App)
// pinia
import { createPinia } from 'pinia'
const pinia = createPinia()

import { createPersistedState } from 'pinia-plugin-persistedstate'
pinia.use(
  createPersistedState({
    auto: true // 启用所有 Store 默认持久化
  })
)

app.use(pinia)
// store--引用index.js
//import store from '@/store/index.js'
// 或者简写
import store from '@/store'

app.config.globalProperties.$store = store

app.mount('#app')
