package repository

import (
	"context"
	"todo/internal/model/entity"
)

type TodoList interface {
	SelectExcludingInbox(ctx context.Context, userID uint) ([]*entity.TodoList, error)
	GetInbox(ctx context.Context, userID uint) (*entity.TodoList, error)
	GetByID(ctx context.Context, id uint) (*entity.TodoList, error)

	Save(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error)
	Update(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error)
	Delete(ctx context.Context, id uint) error
}
