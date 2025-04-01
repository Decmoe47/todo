<template>
  <div class="sidebar">
    <el-button type="info" @click="$emit('close')">关闭</el-button>
    <h3>{{ todo.title || todo.content }}</h3>
    <p><strong>提醒时间：</strong>{{ todo.remindTime || '无' }}</p>
    <p><strong>备注：</strong>{{ todo.note || '无' }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  todo: {
    content: string;
    title?: string;
    remindTime?: string;
    note?: string;
    // ...其他字段...
  } | null
}>()
</script>

<style scoped>
.sidebar {
  width: 400px;
  max-height: 100vh;
  background-color: #fff;
  box-shadow: -2px 0 5px rgba(0,0,0,0.1);
  padding: 20px;
  overflow-y: auto;
}
.close-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  margin-bottom: 10px;
}
</style>
