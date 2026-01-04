package repository

import (
	"context"
	"todo/internal/model/entity"
)

type User interface {
	GetByEmail(ctx context.Context, email string) (*entity.User, error)
	GetByID(ctx context.Context, id uint) (*entity.User, error)

	HasByEmail(ctx context.Context, email string) (bool, error)

	SelectByEmail(ctx context.Context, email string) ([]*entity.User, error)
	SelectByName(ctx context.Context, name string) ([]*entity.User, error)

	Save(ctx context.Context, user *entity.User) error
}
