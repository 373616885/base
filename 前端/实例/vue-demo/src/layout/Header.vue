<template>
  <div class="navbar">
    <Hamburger />
    <Breadcrumb />
    <div class="navbar-right">
      <Driver class="navbar-item" />
      <Screenfull class="navbar-item"></Screenfull>
      <Lang class="navbar-item" />
      <Avatar class="navbar-item" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Hamburger from '@/layout/Hamburger.vue'
import Breadcrumb from '@/layout/Breadcrumb.vue'
import Avatar from '@/layout/Avatar.vue'
import Lang from '@/layout/Lang.vue'
import Screenfull from '@/layout/Screenfull.vue'
import Driver from '@/layout/Driver.vue'
</script>

<style scoped>
.navbar {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  align-items: center;
  position: relative;
  .navbar-right {
    height: 100%;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    .navbar-item {
      height: 100%;
      margin-left: 18px;
      align-items: center;
      color: #666;
      cursor: pointer;
    }
  }
}
</style>
