import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      host: 'localhost', //只能本地访问
      //host: '0.0.0.0', 局域网别人也能看
      port: Number(env.VITE_APP_PORT),
      //open: true,
      proxy: {
        // 前端API http://192.168.9.110:8888/prod-api/api
        // 传后端替换为 http://192.168.9.110:8888/api
        [env.VITE_APP_BASE_API_PATH]: {
          target: 'http://127.0.0.1:8000',
          changeOrigin: true,
          rewrite: (path) => path.replace(env.VITE_APP_BASE_API_PATH, '')
        }
      }
    }
  }
})
