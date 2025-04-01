<template>
  <el-container style="height: 100vh">
    <el-aside class="todo-list-sidebar">
      <TodoListSidebar />
    </el-aside>
    <el-container>
      <el-main style="padding: 0">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import TodoListSidebar from '@/components/TodoListSidebar.vue'
</script>

<style scoped>
.todo-list-sidebar {
  width: 250px;
  box-shadow: 1px 0 3px rgba(0, 0, 0, 0.1);
}
</style>
