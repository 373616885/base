import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { menuList } from '@/api/menu-api'
import { sessionRead, sessionSave } from '@/lib/local-util'

export const useAsideStore = defineStore('aside', () => {
  // 默认激活的菜单
  const defaultActiveMenu = ref<string>(sessionRead('defaultActiveMenu') || '/home')
  // 保存默认激活的菜单
  const saveDefaultActiveMenu = (url: string) => {
    defaultActiveMenu.value = url
    sessionSave('defaultActiveMenu', url)
  }
  // 菜单
  const menus = reactive(JSON.parse(sessionRead('menus') || '[]'))

  // 初始化菜单
  const initMenus = async () => {
    if (menus.length === 0) {
      const result = await menuList()
      sessionSave('menus', JSON.stringify(result))
      // 首次展示的菜单，刷新之后的菜单就是显示的是sessionStorage里的菜单
      for (const item of result) {
        menus.push(item)
      }
    }
  }
  initMenus()

  return { defaultActiveMenu, saveDefaultActiveMenu, menus }
})
