<template>
  <h2>{{ listName }}</h2>

  <div class="todo-list">
    <div class="todo-input">
      <el-input v-model="newTodoContent" placeholder="Add todo" @keyup.enter="addTodo">
        <template #append>
          <el-button @click="addTodo"> Add </el-button>
        </template>
      </el-input>
    </div>

    <div v-if="todos.length > 0">
      <el-card v-for="todo in todos" :key="todo.id" class="todo-item">
        <div class="todo-content">
          <el-checkbox v-model="todo.done" @change="() => toggleTodo(todo.id)" />
          <span :class="{ completed: todo.done }">
            {{ todo.content }}
          </span>
          <el-button type="danger" size="small" icon="Delete" circle @click="deleteTodo(todo.id)" />
        </div>
      </el-card>
    </div>
    <div v-else>
      <span style="color: lightgray">No todo yet</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTodoStore } from '@/stores/todo.ts';
import { useUserStore } from '@/stores/user.ts';
import type { TodoDTO } from '@/types/todo.ts';
import { computed, ref, watchEffect } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute()
const userStore = useUserStore()
const todoStore = useTodoStore()
const listName = ref('')
const todos = ref<TodoDTO[]>([])
const newTodoContent = ref('')

const listId = computed(() => route.params.listId as string)

const addTodo = async () => {
  const todoDTO = await todoStore.addTodo({
    content: newTodoContent.value,
    belongedListId: listId.value,
  })
  todos.value.push(todoDTO)
  newTodoContent.value = ''
}
const toggleTodo = async (id: number) => {
  const todoDTO = await todoStore.toggleTodo(id)
  todos.value = todos.value.map((todo) => (todo.id === id ? todoDTO : todo))
}
const deleteTodo = async (id: number) => {
  await todoStore.deleteTodo(id)
  todos.value = todos.value.filter((todo) => todo.id !== id)
}

watchEffect(async () => {
    if (todoStore.customTodoLists.length === 0) return

    if (userStore.userId) {
      if (listId.value === 'inbox') {
        todos.value = await todoStore.getTodos(userStore.userId, listId.value, true)
        listName.value = 'Inbox'
      } else {
        todos.value = await todoStore.getTodos(userStore.userId, listId.value)
        listName.value = todoStore.getListName(listId.value)
      }
    }
  }
)
</script>

<style scoped>
.todo-list {
  max-width: 800px;
  margin: 0 auto;
}

.todo-input {
  margin-bottom: 20px;
}

.todo-item {
  margin-bottom: 10px;
}

.todo-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.completed {
  text-decoration: line-through;
  color: #999;
}
</style>
