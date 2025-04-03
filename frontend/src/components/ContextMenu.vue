<template>
  <div
    v-show="visible"
    class="context-menu"
    :style="{ left: x + 'px', top: y + 'px' }"
    @mouseleave="onMenuMouseLeave"
    @mouseenter="onMenuMouseEnter"
  >
    <el-menu class="context-menu-inner" mode="vertical">
      <template v-for="(item, key) in menuItems" :key="key">
        <el-menu-item
          :class="{ 'has-children': item.children }"
          @mouseenter="item.children ? onItemMouseEnter($event, key as string) : null"
          @mouseleave="item.children ? onItemMouseLeave() : null"
          @click="!item.children ? handleMenuClick(item) : $event.stopPropagation()"
        >
          <span class="menu-label">{{ item.label }}</span>
          <el-icon v-if="item.children" size="14" class="submenu-arrow">
            <ArrowRight />
          </el-icon>
        </el-menu-item>
      </template>
    </el-menu>
    <div
      v-if="currentSubMenu"
      class="side-sub-menu"
      :style="{ top: subMenuY + 'px' }"
      @mouseenter="onSubMenuMouseEnter"
      @mouseleave="onSubMenuMouseLeave"
    >
      <el-menu mode="vertical" class="side-sub-menu-inner">
        <el-menu-item
          v-for="(child, idx) in currentSubMenu"
          :key="idx"
          @click="handleMenuClick(child)"
          class="side-sub-menu-item"
        >
          {{ child.label }}
        </el-menu-item>
      </el-menu>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import type { MenuItem, MenuItems } from '@/types/menu.ts'
import { ArrowRight } from '@element-plus/icons-vue'
import emitter from '@/utils/eventBus.ts'

const props = defineProps<{
  menuItems: MenuItems
}>()

const visible = ref(false)
const x = ref(0)
const y = ref(0)

const hoveredKey = ref<string>('')
const subMenuY = ref(0)
let hideTimer: number | null = null

const currentSubMenu = computed(() => {
  return hoveredKey.value ? props.menuItems[hoveredKey.value].children : null
})

const handleMenuClick = async (menuItem: MenuItem) => {
  if (menuItem.action) await menuItem.action()
  hide()
}

const onItemMouseEnter = (e: MouseEvent, key: string) => {
  if (hideTimer) clearTimeout(hideTimer)
  hoveredKey.value = key
  subMenuY.value = (e.currentTarget as HTMLElement).offsetTop
}
const onItemMouseLeave = () => {
  hideTimer = window.setTimeout(() => {
    hoveredKey.value = ''
  }, 200)
}

const onSubMenuMouseEnter = () => {
  if (hideTimer) clearTimeout(hideTimer)
}
const onSubMenuMouseLeave = () => {
  hoveredKey.value = ''
}

const onMenuMouseLeave = () => {
  hoveredKey.value = ''
}
const onMenuMouseEnter = () => {
  if (hideTimer) clearTimeout(hideTimer)
}

// 显示隐藏右键菜单
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
  emitter.on('hide-context-menu', hide)
})
onBeforeUnmount(() => {
  window.removeEventListener('click', hide)
  emitter.off('hide-context-menu', hide)
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
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.context-menu-inner {
  border-right: none;
  border-radius: inherit;
  padding: 4px;
}

.menu-label {
  flex: 1;
}

.submenu-arrow {
  margin-left: 3px; /* 箭头与文本间距 */
  margin-right: 0;
  justify-content: end;
}

.side-sub-menu {
  position: absolute;
  left: 100%;
  top: 0;
  z-index: 1000;
  border: 1px solid var(--el-menu-border-color);
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 5px 12px rgba(0, 0, 0, 0.1);
  margin-left: -1px;
}

.side-sub-menu-inner {
  border-radius: inherit;
  padding: 4px;
}

.el-menu-item {
  padding-left: 10px !important;

  height: 34px;
  border-radius: 4px;
  font-size: 13px;
  color: #303133 !important;
}

.el-menu-item.has-children {
  /* 修改有子菜单选项的 padding-right */
  padding-right: 0;
}
</style>
