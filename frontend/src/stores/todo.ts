import axiosInstance from '@/libs/axios.ts'
import type { R } from '@/types/response'
import type { Todo, TodoAddReq, TodoList, TodoMoveReq, TodoUpdateReq } from '@/types/todo.ts'
import { defineStore } from 'pinia'

interface TodoState {
  todoLists: TodoList[]
}

export const useTodoStore = defineStore('todoList', {
  state: (): TodoState => ({
    todoLists: [],
  }),

  actions: {
    /////// todo list ///////
    async loadAllTodoLists() {
      const res = await axiosInstance.get<R<TodoList[]>, R<TodoList[]>>('todoLists/all')
      this.todoLists = res.data
    },

    getTodoList(listId: number): TodoList | undefined {
      return this.todoLists.find((t) => t.id === listId)
    },

    async getCustomLists() {
      const res = await axiosInstance.get<R<TodoList[]>, R<TodoList[]>>('todoLists/custom')
      this.todoLists = res.data
    },

    getListName(listId: number): string {
      return this.todoLists.find((t) => t.id === listId)?.name ?? ''
    },

    async addList(name: string): Promise<TodoList> {
      const res = await axiosInstance.post<R<TodoList>, R<TodoList>>('todoLists/add', {
        name: name,
      })
      this.todoLists.push(res.data)
      return res.data
    },

    async updateListName(listId: number, name: string): Promise<TodoList> {
      const res = await axiosInstance.post<R<TodoList>, R<TodoList>>('todoLists/update', {
        id: listId,
        name: name,
      })
      const index = this.todoLists.findIndex((t) => t.id === listId)
      if (index !== -1) {
        this.todoLists[index] = res.data
      }
      return res.data
    },

    async deleteList(listId: number) {
      await axiosInstance.post('todoLists/delete', {
        id: listId,
        softDeleted: false,
      })
      this.todoLists = this.todoLists.filter((t) => t.id !== listId)
    },

    /////// todo ///////
    async getTodos(listId: number): Promise<Todo[]> {
      const res = await axiosInstance.get<R<Todo[]>, R<Todo[]>>('todos', {
        params: {
          listId: listId,
        },
      })
      return res.data
    },

    async addTodo(todo: TodoAddReq): Promise<Todo> {
      const res = await axiosInstance.post<R<Todo>, R<Todo>>('todos/add', todo)
      return res.data
    },

    async updateTodo(todo: TodoUpdateReq): Promise<Todo> {
      const res = await axiosInstance.post<R<Todo>, R<Todo>>('todos/update', todo)
      return res.data
    },

    async deleteTodo(todoId: number) {
      await axiosInstance.post('todos/delete', {
        id: todoId,
        softDeleted: false,
      })
    },

    async toggleTodo(todoId: number): Promise<Todo> {
      const res = await axiosInstance.post<R<Todo>, R<Todo>>('todos/toggle', {
        id: todoId,
      })
      return res.data
    },

    async moveTodo(todoMoveDTO: TodoMoveReq): Promise<Todo> {
      const res = await axiosInstance.post<R<Todo>, R<Todo>>('todos/move', todoMoveDTO)
      return res.data
    }
  },

  getters: {
    inboxList(): TodoList | undefined {
      return this.todoLists.find((list) => list.inbox)
    },

    customLists(): TodoList[] {
      return this.todoLists.filter((list) => !list.inbox)
    }
  }
})
