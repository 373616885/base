import router from './index'
import { useLoginStore } from '@/stores/login'
import { isWhitelist } from '@/lib/auth'
// 路由守卫
router.beforeEach((to) => {
  const loginStore = useLoginStore()
  // 如果登录，放行
  if (loginStore.token) {
    return true
  }
  // 查看是否在白名单内
  if (isWhitelist(to.path)) {
    return true
  }
  // 跳转login页面
  return '/login'
})
