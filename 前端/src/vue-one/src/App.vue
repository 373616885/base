<template>
  <h3>{{ message }}</h3>
  <String />
  <Html />
  <Attribute />
  <el></el>
</template>

<script setup>
import { ref } from 'vue'
import String from './components/String.vue'
import Html from './components/Html.vue'
import Attribute from './components/Attribute.vue'
import El from './components/El.vue'
const message = ref('基础练习')
</script>
