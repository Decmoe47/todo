package repository

import (
	"context"
	"todo/internal/model/entity"
)

type Todo interface {
	SelectByListID(ctx context.Context, listID uint) ([]*entity.Todo, error)
	GetByID(ctx context.Context, id uint) (*entity.Todo, error)
	SelectByIDs(ctx context.Context, ids []uint) ([]*entity.Todo, error)

	Save(ctx context.Context, todo *entity.Todo) (*entity.Todo, error)
	Update(ctx context.Context, todo *entity.Todo) (*entity.Todo, error)

	SoftDelete(ctx context.Context, id uint) error
	HardDelete(ctx context.Context, id uint) error
	DeleteByBelongedListID(ctx context.Context, belongedListID uint) error
}
