<template>
  <div class="aside">
    <h3>{{ msg }}</h3>
    <Item v-for="n in 4" :key="n"></Item>
  </div>
</template>

<script>
import Item from './Item.vue'
export default {
  data() {
    return {
      msg: 'Aside'
    }
  },
  components: {
    Item
  }
}
</script>

<style scoped>
.aside {
  float: right;
  width: 30%;
  height: 600px;
  border: 5px solid #699;
  text-align: center;
  box-sizing: border-box;
}
</style>
