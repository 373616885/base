<template>
  <BaseSlot>
    <div>
      <h3>插槽标题</h3>
      <p>插槽内容</p>
    </div>
  </BaseSlot>
  <TopSlot>
    <div>
      <h3>插槽续集</h3>
      <!-- 插槽的渲染虽然在子模板，但数据在父模板赋值 -->
      <p>{{ msg }}</p>
    </div>
  </TopSlot>
  <DefaultSlot> </DefaultSlot>
  <NameSlot>
    <!-- 具名插槽 -->
    <template v-slot:head>
      <p>{{ oneMsg }}</p>
    </template>
    <template v-slot:main>
      <p>{{ twoMsg }}</p>
    </template>
    <template #foot>
      <p>{{ threeMsg }}</p>
    </template>
  </NameSlot>
  <AttrSlot v-slot="slotProps">
    <p>{{ text }} -- {{ slotProps.msg }}</p>
  </AttrSlot>
  <AttrSlot2>
    <template #two="obj">
      <p>{{ text }} -- {{ obj.msg2 }}</p>
    </template>
    <template #three="obj">
      <p>{{ text }} -- {{ obj.msg3 }}</p>
    </template>
  </AttrSlot2>
</template>
<script>
import BaseSlot from './components/BaseSlot.vue'
import TopSlot from './components/TopSlot.vue'
import DefaultSlot from './components/DefaultSlot.vue'
import NameSlot from './components/NameSlot.vue'
import AttrSlot from './components/AttrSlot.vue'
import AttrSlot2 from './components/AttrSlot2.vue'

export default {
  data() {
    return {
      msg: '插槽内容',
      oneMsg: 'qin',
      twoMsg: 'qinjp',
      threeMsg: 'qinjiepeng',
      text: '测试'
    }
  },
  components: {
    BaseSlot,
    TopSlot,
    DefaultSlot,
    NameSlot,
    AttrSlot,
    AttrSlot2
  }
}
</script>
