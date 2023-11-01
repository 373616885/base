<template>
  <h3>{{ msg }}</h3>

  <FilterComponent />
</template>

<script setup>
import FilterComponent from './components/FilterComponent.vue'
const msg = '全局过滤器'
</script>
