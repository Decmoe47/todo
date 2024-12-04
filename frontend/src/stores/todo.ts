import { defineStore } from 'pinia'
import type { TodoAddDTO, TodoDTO, TodoListDTO, TodoUpdateDTO } from '@/types/todo.ts'
import axiosInstance from '@/libs/axios.ts'

interface TodoState {
  todoLists: TodoListDTO[]
}

export const useTodoStore = defineStore('todoList', {
  state: (): TodoState => ({
    todoLists: [],
  }),

  actions: {
    async getAllLists(userId: number) {
      this.todoLists = await axiosInstance.get<TodoListDTO[], TodoListDTO[]>('todoLists', {
        params: { userId: userId },
      })
    },
    getListName(listId: string) {
      return this.todoLists.find((t) => t.id === listId)?.name ?? ''
    },

    async getTodos(userId: number, listId: string) {
      return await axiosInstance.get<TodoDTO[], TodoDTO[]>('todos', {
        params: {
          userId: userId,
          listId: listId,
        },
      })
    },
    async addTodo(todo: TodoAddDTO) {
      return await axiosInstance.post<TodoDTO, TodoDTO>('todos/add', todo)
    },
    async updateTodos(todos: TodoUpdateDTO[]) {
      return await axiosInstance.post<TodoDTO[], TodoDTO[]>('todos/update', todos)
    },
    async deleteTodo(todoId: number) {
      await axiosInstance.post('todos/delete', {
        id: todoId,
        softDeleted: false,
      })
    },
    async toggleTodo(todoId: number) {
      return await axiosInstance.post<TodoDTO, TodoDTO>('todos/toggle', {
        id: todoId,
      })
    },
  },

  getters: {
    inboxListId(): string {
      return this.todoLists.find((t) => t.isInbox)!.id
    },
  },
})
