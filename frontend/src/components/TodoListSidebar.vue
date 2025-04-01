<template>
  <div class="sidebar-container">
    <el-menu mode="vertical" :default-active="$route.path" :router="true" class="sidebar-menu">
      <el-menu-item index="/p/inbox" :class="{ 'active-item': $route.path === `/p/inbox` }"> Inbox </el-menu-item>

      <el-divider style="margin: 10px auto" />

      <el-button type="primary" @click="createListModalVisible = true" icon="Plus" class="new-list-button">
        New List
      </el-button>

      <el-menu-item
        v-for="list in todoStore.customTodoLists"
        :key="list.id"
        :index="`/p/${list.id}`"
        @contextmenu="(e: MouseEvent) => onContextMenu(e, list.id)"
        :class="{ 'active-item': $route.path === `/p/${list.id}` }"
      >
        {{ list.name }}
      </el-menu-item>
    </el-menu>

    <!-- 用户信息区域 -->
    <div class="user-info-menu">
      <el-divider style="margin: 10px auto" />

      <el-dropdown @command="handleCommand">
        <div class="user-info-content">
          <el-avatar :size="40" icon="UserFilled" />
          <span class="username">{{ userStore.user!.name }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu style="padding: 5px; border-radius: 5px">
            <el-dropdown-item command="profile" style="border-radius: 5px">Profile</el-dropdown-item>
            <el-dropdown-item command="logout" style="border-radius: 5px" divided>Logout</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 右键菜单 -->
    <ContextMenu ref="contextMenuRef" :menu-items="menuItems" />

    <!-- 新建对话框 -->
    <el-dialog title="Create todo list" v-model="createListModalVisible" width="30%">
      <el-input v-model="newListName" placeholder="Enter a name"></el-input>
      <template #footer>
        <el-button @click="createListModalVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmCreate">OK</el-button>
      </template>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog title="Rename todo list" v-model="renameModalVisible" width="30%">
      <el-input v-model="renameListNewName" placeholder="Enter new name"></el-input>
      <template #footer>
        <el-button @click="renameModalVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmRename">OK</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useTodoStore } from '@/stores/todo.ts'
import ContextMenu from '@/components/ContextMenu.vue'
import type { MenuItem } from '@/types/menu.ts'

const router = useRouter()
const userStore = useUserStore()
const todoStore = useTodoStore()

const contextMenuRef = ref<InstanceType<typeof ContextMenu>>()
const rightClickedListId = ref('')
const createListModalVisible = ref(false) // 控制新建列表对话框显示
const newListName = ref('')
const renameModalVisible = ref(false) // 控制重命名对话框显示
const renameListNewName = ref('') // 存储新的名称

const menuItems: { [key: string]: MenuItem } = {
  rename: {
    label: 'Rename',
    action: async () => {
      renameListNewName.value =
        todoStore.customTodoLists.find((list) => list.id === rightClickedListId.value)?.name || ''
      renameModalVisible.value = true
    },
  },
  delete: {
    label: 'Delete',
    action: async () => {
      await todoStore.deleteList(rightClickedListId.value)
      await router.push('/p/inbox')
    },
  },
}

const onContextMenu = (e: MouseEvent, listId: string) => {
  contextMenuRef.value!.show(e)
  rightClickedListId.value = listId
}

const confirmRename = async () => {
  if (renameListNewName.value) {
    await todoStore.updateListName(rightClickedListId.value, renameListNewName.value)
    renameModalVisible.value = false
  }
}

const confirmCreate = async () => {
  if (newListName.value) {
    await todoStore.addList(newListName.value)
    newListName.value = ''
    createListModalVisible.value = false
  }
}

const handleLogout = async () => {
  await userStore.logout()
  await router.push('/login')
}

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
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: -5px 0 1px rgba(0, 0, 0, 0.1);
}

.sidebar-menu {
  flex: 1;
  overflow: auto;
  padding: 20px 10px 10px 10px;
  min-height: 0;
  border-right: none;
}

.el-menu-item {
  height: 40px;
  border-radius: 5px;
}

.el-menu-item:hover:not(.active-item) {
  background-color: #f5f5f5 !important; /* 修改鼠标移上去的背景色，但不影响当前选中的淡蓝色 */
}

.active-item {
  background-color: #e6f7ff !important;
}

.new-list-button {
  margin: 5px 10px 10px 10px;
}

.user-info-menu {
  padding: 10px 10px;
  flex-shrink: 0;
}

.user-info-content {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info-content:focus,
.el-avatar:focus {
  outline: none;
  border: none;
}

.username {
  margin-left: 10px;
}

.user-info-sub-menu {
  padding: 5px;
  border-radius: 5px;
}
</style>
