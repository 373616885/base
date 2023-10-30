import { createApp } from 'vue'
import App from './App.vue'
import util from '@/utils/util'

const app = createApp(App)

//app.mixin(util)

app.mount('#app')
