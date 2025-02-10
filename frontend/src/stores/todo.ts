import { defineStore } from 'pinia'
import type { TodoAddDTO, TodoDTO, TodoListDTO, TodoUpdateDTO } from '@/types/todo.ts'
import axiosInstance from '@/libs/axios.ts'

interface TodoState {
  customTodoLists: TodoListDTO[]
}

export const useTodoStore = defineStore('todoList', {
  state: (): TodoState => ({
    customTodoLists: [],
  }),

  actions: {
    async getCustomLists(userId: number) {
      this.customTodoLists = await axiosInstance.get<TodoListDTO[], TodoListDTO[]>('todoLists/custom', {
        params: { userId: userId },
      })
    },
    getListName(listId: string) {
      return this.customTodoLists.find((t) => t.id === listId)?.name ?? ''
    },
    async addList(name: string) {
      const list = await axiosInstance.post<TodoListDTO, TodoListDTO>('todoLists/add', {
        name: name,
      })
      this.customTodoLists.push(list)
      return list
    },

    async getTodos(userId: number, listId: string, inbox: boolean = false) {
      return await axiosInstance.get<TodoDTO[], TodoDTO[]>('todos', {
        params: {
          userId: userId,
          listId: listId,
          inbox: inbox,
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
      return this.customTodoLists.find((t) => t.inbox)!.id
    },
  },
})
