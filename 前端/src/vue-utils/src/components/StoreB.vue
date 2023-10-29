<template>
  From B: {{ store.count }}
  <button @click="store.count++">From B: {{ store.count }}</button>
</template>

<script setup>
import { store } from '@/utils/store.js'
</script>
