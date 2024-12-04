<template>
  <el-menu mode="vertical" default-active="currentActiveId" style="height: 100%">
    <el-menu-item v-for="list in todoStore.todoLists" :key="list.id" :route="{ path: `/p/${list.id}` }">
      {{ list.name }}
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useTodoStore } from '@/stores/todo.ts'
import { watchEffect } from 'vue'

const userStore = useUserStore()
const todoStore = useTodoStore()

watchEffect(async () => {
  if (userStore.userId && todoStore.todoLists.length === 0) {
    await todoStore.getAllLists(userStore.userId)
  }
})
</script>
