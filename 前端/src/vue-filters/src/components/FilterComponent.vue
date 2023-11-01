<template>
  <h3>{{ $filters.sexName(1) }}</h3>
  <p v-for="(item, index) of sexList" :key="index">{{ item }}</p>
</template>
<script setup>
import { inject } from 'vue'
const sexList = inject('sexList')
</script>
