export interface TodoDTO {
  id: number
  content: string
  done: boolean
  dueDate?: Date
  belongedList: TodoListDTO

  createdTime: string
  updatedTime: string
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

export interface TodoListDTO {
  id: string
  name: string
  isInbox: boolean
}
