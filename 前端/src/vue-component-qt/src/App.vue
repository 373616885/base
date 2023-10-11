<template>
  <!-- <Header />
  <Main />
  <Aside />
  <Foot />
  <Parent /> -->
  <ComponentA />
</template>

<script>
import Header from './page/Header.vue'
import Main from './page/Main.vue'
import Aside from './page/Aside.vue'
import Parent from './page/Parent.vue'

import ComponentA from './components/ComponentA.vue'
export default {
  components: {
    // Header: Header,
    // Main: Main,
    // Aside,
    // Parent,
    ComponentA
  }
}
</script>

<style></style>
