import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import ReactivityTransform from ' @vue-macros/reactivity-transform'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    ReactivityTransform(),
    AutoImport({
      imports: ['vue', 'vue-router']
    })
  ],
  build: {
    rollupOptions: {
      output: {
        // manualChunks: {
        //   vue: ['vue']
        // }
        manualChunks(id) {
          console.log(id)
          if (id.includes('node_modules')) {
            return 'vendor'
          }
        }
      }
    }
  }
})
