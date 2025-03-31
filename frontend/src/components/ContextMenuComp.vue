<template>
  <div
    v-show="visible"
    class="context-menu"
    :style="{
      left: x + 'px',
      top: y + 'px',
    }"
  >
    <el-menu class="context-menu-inner">
      <el-menu-item
        v-for="(item, key) in menuItemNames"
        :key="key"
        @click="() => handleMenuClick(key as string)"
        :index="key"
      >
        {{ item }}
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

defineProps<{
  menuItemNames: { [key: string]: string }
}>()
const emits = defineEmits<{ (event: 'menuClick', action: string): void }>()

const visible = ref(false)
const x = ref(0)
const y = ref(0)

const handleMenuClick = (action: string) => {
  emits('menuClick', action)
  hide()
}
const show = (e: MouseEvent) => {
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
  hide,
})
</script>

<style scoped>
.context-menu {
  position: fixed;
  z-index: 999;
  border: 1px solid var(--el-menu-border-color);
  border-radius: 5px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.context-menu-inner {
  border-right: none;
  border-radius: inherit;
  padding: 4px;
}

.el-menu-item {
  height: 34px;
  border-radius: 5px;
  font-size: 13px;
  color: #303133 !important;
}
</style>
