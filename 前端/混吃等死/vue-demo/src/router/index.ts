//创建一个路由器并暴露出去
//第一步：引入vue-router 和 createWebHistory
import { createRouter, createWebHashHistory } from 'vue-router'

//第二步： 引入路由组件
import Index from '@/layout/Index.vue'
import Login from '@/views/Login.vue'

import P404 from '@/views/P404.vue'
import Home from '@/views/Home.vue'
import Data from '@/views/Data.vue'

import UserList from '@/views/user/List.vue'
import UserAdd from '@/views/user/Add.vue'

import SystemRole from '@/views/system/Role.vue'
import SystemMenu from '@/views/system/Menu.vue'
import SystemPermission from '@/views/system/Permission.vue'

//第三步：创建路由器实例
const router = createRouter({
  history: createWebHashHistory(import.meta.env.VITE_APP_BASE_URL), // 路由的工作模式
  // 路由-没有r，router 这个是路由器
  routes: [
    {
      //这里必须有 /
      path: '/login',
      name: 'login',
      component: Login,
      meta: {
        title: '登录'
      }
    },
    {
      //这里必须有 /
      path: '/404',
      name: '404',
      component: P404
    },
    {
      //这里必须有 /
      path: '/',
      name: 'Index',
      component: Index,
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'Home',
          component: Home
        },
        {
          path: 'user/list',
          name: 'UserList',
          component: UserList
        },
        {
          path: 'user/add',
          name: 'UserAdd',
          component: UserAdd
        },
        {
          path: 'system/role',
          name: 'SystemRole',
          component: SystemRole
        },
        {
          path: 'system/menu',
          name: 'SystemMenu',
          component: SystemMenu
        },
        {
          path: 'system/permission',
          name: 'SystemPermission',
          component: SystemPermission
        },
        {
          path: 'data',
          name: 'Data',
          component: Data
        }
      ]
    },
    // 未知路由重定向
    {
      path: '/:pathMatch(.*)',
      redirect: '/404'
    }
  ]
})

//第四步：暴露路由器
export default router
