package entity

import (
	"time"

	"gorm.io/gorm"
)

type Todo struct {
	*Auditable
	Content     string
	DueDate     *time.Time
	Done        bool
	Description *string

	BelongedListID uint
	BelongedList   *TodoList `gorm:"foreignKey:BelongedListID"`
}

func NewTodo(content string, dueDate *time.Time, belongedListID uint, createdByID uint) *Todo {
	return &Todo{
		Auditable: &Auditable{
			Model:       &gorm.Model{},
			CreatedByID: createdByID,
		},
		Content:        content,
		DueDate:        dueDate,
		Done:           false,
		BelongedListID: belongedListID,
	}
}
