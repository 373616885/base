<template>
  <h1>{{ messageObj }}</h1>
  <h1>{{ sexList }}</h1>
  <button @click="handleClick">click</button>
</template>

<script>
import { messageObj } from '@/utils/util.js'
import util from '@/utils/util.js'
console.log(messageObj)
export default {
  mixins: [util],
  data() {
    return {
      messageObj: messageObj
    }
  }
}
</script>
