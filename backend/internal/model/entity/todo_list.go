package entity

import "gorm.io/gorm"

type TodoList struct {
	*Auditable
	Name  string
	Inbox bool

	Todos []*Todo `gorm:"foreignKey:BelongedListID"`
}

func NewTodoList(name string, createdByID uint, inbox bool) *TodoList {
	return &TodoList{
		Auditable: &Auditable{
			Model:       &gorm.Model{},
			CreatedByID: createdByID,
		},
		Name:  name,
		Inbox: inbox,
	}
}
