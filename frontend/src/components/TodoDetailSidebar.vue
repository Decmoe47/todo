<template>
  <div class="sidebar">
    <div class="todo-edit-container">
      <div class="todo-content-container">
        <el-input
          v-model="editableTodo.content"
          @blur="updateTodo"
          placeholder="Enter something..."
          style="height: 35px"
          :input-style="{ fontSize: '18px', fontWeight: 'bold' }"
        >
          <template #prepend>
            <el-checkbox v-model="editableTodo.done" @change="$emit('toggleTodo', editableTodo.id)" :size="'large'" />
          </template>
        </el-input>
      </div>

      <el-divider style="margin: 5px 0" />

      <div class="shadow-hover">
        <el-date-picker
          v-model="editableTodo.dueDate"
          type="datetime"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          placeholder="Add due date"
          @blur="updateTodo"
          style="width: 100%"
        />
      </div>

      <div class="shadow-hover">
        <el-input
          type="textarea"
          v-model="editableTodo.description"
          @blur="updateTodo"
          placeholder="Add description"
          :autosize="{ minRows: 3, maxRows: 25 }"
          :resize="'none'"
          :input-style="{ boxShadow: 'none' }"
        />
      </div>
    </div>

    <el-button type="info" @click="$emit('close')">Close</el-button>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user';
import type { Todo } from '@/types/todo.ts';
import { reactive, watch } from 'vue';

const userStore = useUserStore();
const props = defineProps<{ todo: Todo | null }>()
const emit = defineEmits<{
  close: []
  toggleTodo: [todoId: number]
  updateTodo: [todo: Todo]
}>()

const defaultTodo: Todo = {
  id: -1,
  content: '',
  done: false,
  belongedListId: 0,
  createdBy: userStore.userId || 0,
  createdAt: new Date()
}

// 本地编辑状态
const editableTodo = reactive<Todo>(props.todo ? { ...props.todo } : { ...defaultTodo })

// 当props.todo变化时更新本地状态
watch(
  () => props.todo,
  (newVal) => {
    Object.assign(editableTodo, newVal)
  },
)

// 自动更新函数
const updateTodo = () => {
  emit('updateTodo', editableTodo)
}
</script>

<style scoped>
.sidebar {
  width: 400px;
  max-height: 100vh;
  box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.todo-edit-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__wrapper),
:deep(.el-textarea__inner),
:deep(.el-input-group__prepend) {
  box-shadow: none;
}

.todo-content-container:hover {
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  transition: var(--el-transition-box-shadow);
}

.shadow-hover:hover {
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  transition: var(--el-transition-box-shadow);
}

:deep(.el-input-group__prepend) {
  background-color: #ffffff !important;
  padding-right: 5px;
}
</style>
