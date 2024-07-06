<template>
  <h3>_Styple绑定</h3>
  <p :style="{ color: 'red', 'font-size': '30px' }">style绑定</p>
  <!-- 推荐驼峰命名法：不然要写 单引号括起来  -->
  <p :style="{ color: redColor, fontSize: fSize }">style绑定</p>
  <!-- 实际开发中推荐绑定对象的形式  -->
  <p :style="styleObject">style绑定</p>
  <!-- 实际开发中不推荐绑定数组的形式  -->
  <p :style="[styleObject]">style绑定</p>
  <p :style="[arrColor, arrFontSize]">style绑定</p>
</template>
<script>
export default {
  data() {
    return {
      redColor: 'red',
      fSize: '30px',
      styleObject: {
        color: 'green',
        fontSize: '30px'
      },
      arrColor: {
        color: 'red'
      },
      arrFontSize: {
        fontSize: '30px'
      }
    }
  }
}
</script>
