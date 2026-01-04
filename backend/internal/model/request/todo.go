package request

import "time"

type TodoAdd struct {
	Content        string     `json:"content" binding:"required"`
	DueDate        *time.Time `json:"dueDate" time_format:"2006-01-02T15:04"`
	BelongedListID string     `json:"belongedListId" binding:"required"`
}

type TodoDelete struct {
	ID          uint  `json:"id" binding:"required"`
	SoftDeleted *bool `json:"softDeleted" binding:"required"`
}

type TodoUpdate struct {
	ID          uint       `json:"id" binding:"required"`
	Content     string     `json:"content" binding:"required"`
	Done        *bool      `json:"done" binding:"required"`
	DueDate     *time.Time `json:"dueDate" time_format:"2006-01-02T15:04"`
	Description *string    `json:"description"`
}

type TodoToggle struct {
	ID uint `json:"id" binding:"required"`
}

type TodoMove struct {
	ID           uint   `json:"id" binding:"required"`
	TargetListID string `json:"targetListId" binding:"required"`
}
