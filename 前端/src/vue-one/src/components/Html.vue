<template>
  <h3>{{ message }}</h3>
  <p>Using text interpolation: {{ rawHtml }}</p>
  <p>Using v-html directive: <span v-html="rawHtml"></span></p>
</template>
<script setup>
import { ref } from 'vue'
const message = ref('原始 HTML')
const rawHtml = ref('<span style="color: red">This should be red.</span>')
</script>
