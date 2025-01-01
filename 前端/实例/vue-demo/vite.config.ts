import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { resolve } from 'path'

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import vue from '@vitejs/plugin-vue'

import { visualizer } from 'rollup-plugin-visualizer'

//console.log(import.meta.env.VITE_NODE_ENV)
//console.log(process.env.VITE_NODE_ENV)
//console.log(process.env.NODE_ENV)
const isDev = process.env.NODE_ENV !== 'production'
console.log(isDev)
// https://vitejs.dev/config/
export default defineConfig({
  base: isDev ? '/' : './',
  plugins: [
    vue(),
    createSvgIconsPlugin({
      // 这个是自己配置的图标路径，指出来（自己咋配置的咋来）
      iconDirs: [resolve(process.cwd(), 'src/assets/svg')],
      // 这个表示id，按这个来就对了
      symbolId: 'icon-[dir]-[name]'
    }),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()]
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },

  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  //  主要看的就是这个 build 的部分
  build: {
    minify: true, // false = 是设置打包后的文件不压缩，方便查看, true = 是设置打包后的文件压缩，减小体积
    rollupOptions: {
      output: {
        // 打包输出的配置
        manualChunks: (id) => {
          console.log(id)
          // 这个ID，就是所有文件的绝对路径
          // if (id.includes('node_modules/masonry')) {
          //   return 'masonry'
          // }

          if (id.includes('node_modules/element-plus')) {
            return 'element-plus'
          }
          if (id.includes('node_modules/axios')) {
            return 'axios'
          }
          if (id.includes('node_modules/driver')) {
            return 'driver'
          }
          if (id.includes('node_modules/lodash-es')) {
            return 'lodash-es'
          }
          if (id.includes('node_modules')) {
            // 因为 node_modules 中的依赖通常是不会改变的
            // 所以直接单独打包出去
            // 这个return 的值就是打包的名称
            return 'vendor'
          }
        }
      },
      plugins: [visualizer()]
    }
  }
})
