<template>
  <div class="page-container">
    <div class="main-content">
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
            @click="openDetail(todo)"
            @contextmenu="(e: MouseEvent) => onContextMenu(e, todo.id)"
            :class="['todo-item', { selected: selectedTodo && selectedTodo.id === todo.id }]"
          >
            <div class="todo-content">
              <el-checkbox v-model="todo.done" @change="() => toggleTodo(todo.id)" />
              <span :class="{ completed: todo.done }">
                {{ todo.content }}
              </span>
            </div>
          </el-card>

          <!-- 右键菜单 -->
          <ContextMenu ref="contextMenuRef" :menu-items="menuItems" />
        </div>
        <div v-else>
          <span style="color: lightgray">No todo yet</span>
        </div>
      </div>
    </div>

    <!-- 侧边栏 -->
    <TodoDetailSidebar
      v-if="isSidebarOpen"
      :todo="selectedTodo"
      @close="closeSidebar"
      @toggleTodo="toggleTodo"
      @updateTodo="updateTodo"
    />
  </div>
</template>

<script setup lang="ts">
import ContextMenu from '@/components/ContextMenu.vue'
import TodoDetailSidebar from '@/components/TodoDetailSidebar.vue'
import { useTodoStore } from '@/stores/todo.ts'
import { useUserStore } from '@/stores/user.ts'
import type { MenuItems } from '@/types/menu.ts'
import type { Todo } from '@/types/todo.ts'
import emitter from '@/utils/eventBus.ts'
import { computed, ref, watch, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const userStore = useUserStore()
const todoStore = useTodoStore()
const listName = ref('')
const todos = ref<Todo[]>([])
const newTodoContent = ref('')
const contextMenuRef = ref<InstanceType<typeof ContextMenu>>()
const rightClickedTodoId = ref(0)
const listId = computed(() =>
  route.params.listId === 'inbox' ? todoStore.inboxList?.id : Number(route.params.listId),
)
const selectedTodo = ref<Todo | null>(null)
const isSidebarOpen = ref(false)

const menuItems = computed<MenuItems>(() => ({
  move: {
    label: 'Move to',
    children: {
      ...todoStore.todoLists.reduce((acc: MenuItems, list) => {
        acc[list.id] = {
          label: list.name,
          action: async () => {
            await todoStore.moveTodo(
              {
                id: rightClickedTodoId.value,
                targetListId: list.id,
              },
            )
            todos.value = todos.value.filter((t) => t.id !== rightClickedTodoId.value)
          },
          disabled: (() => {
            const todo = todos.value.find((t) => t.id === rightClickedTodoId.value)
            return todo && todo.belongedListId === list.id
          })(),
        }
        return acc
      }, {}),
    },
  },
  delete: {
    label: 'Delete',
    action: async () => {
      await todoStore.deleteTodo(rightClickedTodoId.value)
      todos.value = todos.value.filter((todo) => todo.id !== rightClickedTodoId.value)
    },
  },
}))

const onContextMenu = (e: MouseEvent, todoId: number) => {
  emitter.emit('hide-context-menu')
  contextMenuRef.value!.show(e)
  rightClickedTodoId.value = todoId
}

const addTodo = async () => {
  if (listId.value == null) return
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

const updateTodo = async (todo: Todo) => {
  const newTodo = await todoStore.updateTodo(todo)
  if (!newTodo) return
  todos.value = todos.value.map((t) => (t.id === newTodo.id ? newTodo : t))
}

// 点击 todo 打开侧边栏
const openDetail = (todo: Todo) => {
  selectedTodo.value = todo
  isSidebarOpen.value = true
}

const closeSidebar = () => (isSidebarOpen.value = false)

watchEffect(async () => {
  if (!userStore.userId) return
  if (listId.value == null) return

  if (route.params.listId === 'inbox') {
    listName.value = 'Inbox'
  } else {
    listName.value = todoStore.getListName(listId.value)
  }

  todos.value = await todoStore.getTodos(listId.value)
})

watch(listId, () => {
  selectedTodo.value = null
  isSidebarOpen.value = false
})
</script>

<style scoped>
.page-container {
  display: flex;
  height: 100vh;
  background-color: #faf9f8;
}
.main-content {
  flex: 1;
  padding: 24px;
}

.todo-list-title {
  font-size: 28px;
  margin-top: 10px;
  margin-bottom: 30px;
}

.todo-list {
  width: 100%;
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

/* 鼠标悬停未选中时变为淡灰色 */
.todo-item:not(.selected):hover {
  background-color: #f5f5f5;
}
/* 选中状态时的淡蓝色 */
.todo-item.selected {
  background-color: #e6f7ff;
}
</style>
