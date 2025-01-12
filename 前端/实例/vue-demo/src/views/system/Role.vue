<template>
  <el-tree style="max-width: 600px" :props="props" :load="loadNode" lazy show-checkbox />
  <h3>name</h3>
  <h4 v-html="content"></h4>
</template>

<script lang="ts" setup>
import type Node from 'element-plus/es/components/tree/src/model/node'
import { getRole } from '@/api/role-api'
import { ref } from 'vue'

const content = ref('')
getRole({ id: 1 }).then((res: any) => {
  console.log(res)
  content.value = res
})

interface Tree {
  name: string
  leaf?: boolean
}

const props = {
  label: 'name',
  children: 'zones',
  isLeaf: 'leaf'
}

const loadNode = (node: Node, resolve: (data: Tree[]) => void) => {
  if (node.level === 0) {
    return resolve([{ name: 'region' }])
  }
  if (node.level > 1) return resolve([])

  setTimeout(() => {
    const data: Tree[] = [
      {
        name: 'leaf',
        leaf: true
      },
      {
        name: 'zone'
      }
    ]

    resolve(data)
  }, 500)
}
</script>
