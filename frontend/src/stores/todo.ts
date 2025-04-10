import { defineStore } from 'pinia'
import type { TodoAddDTO, TodoDTO, TodoListDTO, TodoMoveDTO, TodoUpdateDTO } from '@/types/todo.ts'
import axiosInstance from '@/libs/axios.ts'

interface TodoState {
  customTodoLists: TodoListDTO[]
}

export const useTodoStore = defineStore('todoList', {
  state: (): TodoState => ({
    customTodoLists: [],
  }),

  actions: {
    /////// todo list ///////
    async getCustomLists() {
      this.customTodoLists = await axiosInstance.get<TodoListDTO[], TodoListDTO[]>('todoLists/custom')
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

    async updateListName(listId: string, name: string) {
      const list = await axiosInstance.post<TodoListDTO, TodoListDTO>('todoLists/update', {
        id: listId,
        name: name,
      })
      const index = this.customTodoLists.findIndex((t) => t.id === listId)
      if (index !== -1) {
        this.customTodoLists[index] = list
      }
      return list
    },

    async deleteList(listId: string) {
      await axiosInstance.post('todoLists/delete', {
        id: listId,
        softDeleted: false,
      })
      this.customTodoLists = this.customTodoLists.filter((t) => t.id !== listId)
    },

    /////// todo ///////
    async getTodos(listId: string) {
      return await axiosInstance.get<TodoDTO[], TodoDTO[]>('todos', {
        params: {
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

    async deleteTodos(todoIds: number[]) {
      const data: { id: number; softDeleted: boolean }[] = []
      todoIds.forEach((id) => {
        data.push({
          id: id,
          softDeleted: false,
        })
      })
      await axiosInstance.post('todos/delete', data)
    },

    async toggleTodo(todoId: number) {
      return await axiosInstance.post<TodoDTO, TodoDTO>('todos/toggle', {
        id: todoId,
      })
    },

    async moveTodos(todoMoveDTOs: TodoMoveDTO[]) {
      return await axiosInstance.post<TodoDTO[], TodoDTO[]>('todos/move', todoMoveDTOs)
    },
  },

  getters: {
    inboxListId(): string {
      return this.customTodoLists.find((t) => t.inbox)!.id
    },
  },
})
