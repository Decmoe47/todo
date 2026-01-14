
// Data interfaces
export interface Todo {
  id: number
  content: string
  done: boolean
  dueDate?: Date
  description?: string

  belongedListId: number
  createdBy: number
  createdAt: Date
  updatedBy?: number
  updatedAt?: Date
}

export interface TodoList {
  id: number
  name: string
  inbox: boolean

  createdBy: Number
  createdAt: Date
  updatedBy: Number
  updatedAt: Date
}

// Request interfaces
export interface TodoAddReq {
  content: string
  dueDate?: Date
  belongedListId: number
}

export interface TodoUpdateReq {
  id: number
  content: string
  done: boolean
  dueDate?: Date
}

export interface TodoMoveReq {
  id: number
  targetListId: number
}
