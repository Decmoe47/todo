package request

type TodoListAdd struct {
	Name string `json:"name" binding:"required"`
}

type TodoListUpdate struct {
	ID   uint   `json:"id" binding:"required"`
	Name string `json:"name" binding:"required"`
}

type TodoListDelete struct {
	ID uint `json:"id" binding:"required"`
}
