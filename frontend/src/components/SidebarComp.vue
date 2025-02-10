<template>
  <el-menu mode="vertical" :default-active="$route.path" :router="true" style="height: 100%">
    <div style="padding: 10px 10px 10px 0">
      <el-menu-item index="/p/inbox">Inbox</el-menu-item>
      <el-divider />
      <div style="padding: 0 20px 10px 10px; margin-bottom: 10px">
        <el-button type="primary" @click="showNewListInput" icon="Plus">New List</el-button>
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
      <el-menu-item v-for="list in todoStore.customTodoLists" :key="list.id" :index="`/p/${list.id}`">
        {{ list.name }}
      </el-menu-item>
    </div>
  </el-menu>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useTodoStore } from '@/stores/todo.ts'
import { nextTick, ref, watchEffect } from 'vue'

const userStore = useUserStore()
const todoStore = useTodoStore()
const newListNameInputShown = ref(false)
const newListName = ref('')
const newListNameInput = ref()

watchEffect(async () => {
  if (userStore.userId && todoStore.customTodoLists.length === 0) {
    await todoStore.getCustomLists(userStore.userId)
  }
})

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
</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #d3daf3 !important;
  color: #000000;
}
</style>
