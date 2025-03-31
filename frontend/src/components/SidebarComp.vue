<template>
  <div class="sidebar-container">
    <el-menu mode="vertical" :default-active="$route.path" :router="true" class="sidebar-menu">
      <el-menu-item index="/p/inbox">Inbox</el-menu-item>
      <el-divider />
      <div style="padding: 0 20px 10px 10px; margin-bottom: 10px">
        <el-button type="primary" @click="showNewListInput" icon="Plus"> New List </el-button>
        <el-input
          ref="newListNameInput"
          v-show="newListNameInputShown"
          v-model="newListName"
          placeholder="Enter list name"
          @keyup.enter="createNewList"
          @blur="hideNewListInput"
          style="margin-top: 5px"
        />
      </div>
      <el-menu-item
        v-for="list in todoStore.customTodoLists"
        :key="list.id"
        :index="`/p/${list.id}`"
        @contextmenu="(e) => onContextMenu(e, list.id)"
      >
        {{ list.name }}
      </el-menu-item>
    </el-menu>

    <!-- 用户信息区域 -->
    <div class="user-info">
      <el-dropdown @command="handleCommand">
        <div class="user-info-content">
          <el-avatar :size="40" icon="UserFilled" />
          <span class="username">{{ userStore.user!.name }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">Profile</el-dropdown-item>
            <el-dropdown-item command="logout" divided>Logout</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <SidebarContextMenuComp ref="contextMenuRef" @menu-click="handleMenuClick" />
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useTodoStore } from '@/stores/todo.ts'
import { nextTick, ref, watchEffect } from 'vue'
import SidebarContextMenuComp from '@/components/SidebarContextMenuComp.vue'
import type { SidebarContextMenuOption } from '@/types/todo.ts'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const todoStore = useTodoStore()
const newListNameInputShown = ref(false)
const newListName = ref('')
const newListNameInput = ref()
const contextMenuRef = ref()
const rightClickedListId = ref('')
const showUserMenu = ref(false)

const onContextMenu = (e: MouseEvent, listId: string) => {
  contextMenuRef.value.show(e)
  rightClickedListId.value = listId
}
const handleMenuClick = (action: SidebarContextMenuOption) => {
  switch (action) {
    case 'rename':
      console.log(rightClickedListId.value)
      break
    case 'delete':
      break
  }
}
const showNewListInput = async () => {
  newListNameInputShown.value = true
  await nextTick()
  newListNameInput.value.focus()
}
const hideNewListInput = () => {
  newListNameInputShown.value = false
  newListName.value = ''
}
const createNewList = async () => {
  if (newListName.value) {
    await todoStore.addList(newListName.value)
    newListName.value = ''
    newListNameInputShown.value = false
  }
}

const handleLogout = async () => {
  showUserMenu.value = false
  await userStore.logout()
  await router.push('/login')
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      break
    case 'logout':
      handleLogout()
      break
  }
}

watchEffect(async () => {
  if (userStore.userId && todoStore.customTodoLists.length === 0) {
    await todoStore.getCustomLists(userStore.userId)
  }
})
</script>

<style scoped>
.sidebar-container {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-menu {
  flex: 1;
  overflow: auto;
}

.user-info {
  padding: 16px;
  border-top: 1px solid var(--el-menu-border-color);
  border-right: 1px solid var(--el-menu-border-color);
}

.user-info-content {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 10px;
}
</style>
