**第一步：把你的el-menu组件加上属性 :collapse-transition=“false”**

```vue
<template>
  <div >
    <el-menu :router="true" unique-opened :collapse="isCollapse" :collapse-transition="false">
          <!-- 菜单栏的标题 -->
              <div class="title " v-if="isCollapse">ha</div>
              <div class="title" v-else>我的后台系统</div>
              <!-- 菜单组件 -->
        <menus v-for="(item,index) in menuData" :key="index" :menu="item" ></menus>
    </el-menu>
  </div>
</template>

```



**第二步：到你的main.vue或者是layout.vue组件，也就是主出口组件内加样式**

```vue
html部分----
<el-aside>
        <!-- 菜单组件 -->
          <nav-left></nav-left>
</el-aside>
//给你的el-aside组件加上这个动画效果，然后你在点击试试就有效果了。css部分----
<style scoped>
  .el-aside {
    transition: width 0.35s;
  -webkit-transition: width 0.35s;
  -moz-transition: width 0.35s;
  -webkit-transition: width 0.35s;
  -o-transition: width 0.35s;
  }
</style>
```

