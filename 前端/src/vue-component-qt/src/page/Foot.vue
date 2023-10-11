<template>
  <div class="clear"></div>
  <div class="foot">
    <h3>{{ msg }}</h3>
  </div>
</template>
<script>
export default {
  data() {
    return {
      msg: 'Foot'
    }
  }
}
</script>
<style scoped>
.clear {
  clear: both;
}
.foot {
  width: 100%;
  margin-top: 10px;
  border: 5px solid #999;
}
</style>
