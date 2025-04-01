<template>
  <h2 class="todo-list-title">{{ listName }}</h2>

  <div class="todo-list">
    <!-- todo 添加框 -->
    <div class="todo-input">
      <el-input v-model="newTodoContent" placeholder="Add todo" @keyup.enter="addTodo">
        <template #append>
          <el-button @click="addTodo" type="primary" icon="Plus"></el-button>
        </template>
      </el-input>
    </div>

    <!-- todo 列表 -->
    <div v-if="todos.length > 0">
      <el-card
        v-for="todo in todos"
        :key="todo.id"
        @contextmenu="(e: MouseEvent) => onContextMenu(e, todo.id)"
        class="todo-item"
      >
        <div class="todo-content">
          <el-checkbox v-model="todo.done" @change="() => toggleTodo(todo.id)" />
          <span :class="{ completed: todo.done }">
            {{ todo.content }}
          </span>
        </div>
      </el-card>

      <ContextMenuComp ref="contextMenuRef" :menu-items="menuItems" />
    </div>
    <div v-else>
      <span style="color: lightgray">No todo yet</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTodoStore } from '@/stores/todo.ts'
import { useUserStore } from '@/stores/user.ts'
import type { TodoDTO } from '@/types/todo.ts'
import { computed, ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import ContextMenuComp from '@/components/ContextMenuComp.vue'
import type { MenuItem } from '@/types/menu.ts'

const route = useRoute()
const userStore = useUserStore()
const todoStore = useTodoStore()
const listName = ref('')
const todos = ref<TodoDTO[]>([])
const newTodoContent = ref('')
const contextMenuRef = ref<InstanceType<typeof ContextMenuComp>>()
const rightClickedTodoId = ref(0)
const listId = computed(() => route.params.listId as string)

const menuItems: { [key: string]: MenuItem } = {
  rename: {
    label: 'Rename',
    action: async () => {},
  },
  delete: {
    label: 'Delete',
    action: async () => {
      await todoStore.deleteTodos([rightClickedTodoId.value])
      todos.value = todos.value.filter((todo) => todo.id !== rightClickedTodoId.value)
    },
  },
}

const onContextMenu = (e: MouseEvent, todoId: number) => {
  contextMenuRef.value!.show(e)
  rightClickedTodoId.value = todoId
}

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

watchEffect(async () => {
  if (todoStore.customTodoLists.length === 0) return

  if (userStore.userId) {
    if (listId.value === 'inbox') {
      listName.value = 'Inbox'
    } else {
      listName.value = todoStore.getListName(listId.value)
    }
    todos.value = await todoStore.getTodos(userStore.userId, listId.value)
  }
})
</script>

<style scoped>
.todo-list-title {
  font-size: 28px;
  margin-top: 0;
  margin-bottom: 20px;
}

.todo-list {
  max-width: 800px;
}

.todo-input {
  margin-bottom: 20px;
}

.todo-item {
  height: 40px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}

.todo-item .el-card__body {
  padding: 0;
  display: flex;
  align-items: center;
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
