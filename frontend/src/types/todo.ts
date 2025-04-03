import type { UserDTO } from '@/types/user.ts'

export interface TodoDTO extends BaseTodoDTO {
  belongedList: TodoListDTO
  createdBy: UserDTO
  updatedBy: UserDTO
  createdTime: Date
  updatedTime: Date
}

export interface BaseTodoDTO {
  id: number
  content: string
  done: boolean
  dueDate?: Date
  description?: string
}

export interface TodoAddDTO {
  content: string
  dueDate?: Date
  belongedListId: string
}

export interface TodoUpdateDTO {
  id: number
  content: string
  done: boolean
  dueDate?: Date
}

export interface TodoMoveDTO {
  id: number
  targetListId: string
}

export interface TodoListDTO {
  id: string
  name: string
  inbox: boolean
}
