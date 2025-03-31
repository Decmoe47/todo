<template>
  <div
    v-show="visible"
    class="context-menu"
    :style="{
      left: x + 'px',
      top: y + 'px'
    }"
  >
    <el-menu>
      <el-menu-item v-for="(item, key) in menuItems" :key="key" @click="() => handleMenuClick(key)" :index="key">
        {{ item }}
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import type { SidebarContextMenuOption } from '@/types/todo.ts'

const emits = defineEmits<{ (event: 'menuClick', action: SidebarContextMenuOption): void }>()

const visible = ref(false)
const x = ref(0)
const y = ref(0)
const menuItems = {
  rename: '重命名',
  delete: '删除'
}

const handleMenuClick = (action: SidebarContextMenuOption) => {
  emits('menuClick', action)
  hide()
}
const show = (e) => {
  e.preventDefault()
  x.value = e.clientX
  y.value = e.clientY
  visible.value = true
}
const hide = () => {
  visible.value = false
}

onMounted(() => {
  window.addEventListener('click', hide)
})
onBeforeUnmount(() => {
  window.removeEventListener('click', hide)
})

defineExpose({
  show,
  hide
})
</script>

<style scoped>
.context-menu {
  position: fixed;
  z-index: 999;
}
</style>
