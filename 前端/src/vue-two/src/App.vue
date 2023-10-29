<template>
  <button>package</button>
  <Com />
</template>

<script setup>
import Com from './components/Com.vue'
console.log(import.meta.env.VITE_APP_NAME)
console.log(import.meta.env.VITE_APP_BASE_API)
</script>
