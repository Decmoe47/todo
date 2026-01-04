package impl

import (
	"context"
	"todo/internal/model/entity"
	"todo/internal/repository"

	"github.com/cockroachdb/errors"
	"gorm.io/gorm"
)

type UserRepository struct {
	db *gorm.DB
}

func NewUserRepository(db *gorm.DB) *UserRepository {
	return &UserRepository{db: db}
}

func (r *UserRepository) GetByID(ctx context.Context, id uint) (*entity.User, error) {
	user, err := gorm.G[*entity.User](repository.DBFromCtx(ctx, r.db)).Where("id = ?", id).First(ctx)
	return user, errors.WithStack(err)
}

func (r *UserRepository) GetByEmail(ctx context.Context, email string) (*entity.User, error) {
	user, err := gorm.G[*entity.User](repository.DBFromCtx(ctx, r.db)).Where("email = ?", email).First(ctx)
	return user, errors.WithStack(err)
}
func (r *UserRepository) HasByEmail(ctx context.Context, email string) (bool, error) {
	count, err := gorm.G[*entity.User](repository.DBFromCtx(ctx, r.db)).Where("email = ?", email).Count(ctx, "id")
	return count > 0, errors.WithStack(err)
}

func (r *UserRepository) SelectByEmail(ctx context.Context, email string) ([]*entity.User, error) {
	users, err := gorm.G[*entity.User](repository.DBFromCtx(ctx, r.db)).Where("email = ?", email).Find(ctx)
	return users, errors.WithStack(err)
}

func (r *UserRepository) SelectByName(ctx context.Context, name string) ([]*entity.User, error) {
	users, err := gorm.G[*entity.User](repository.DBFromCtx(ctx, r.db)).Where("name = ?", name).Find(ctx)
	return users, errors.WithStack(err)
}

func (r *UserRepository) Save(ctx context.Context, user *entity.User) error {
	err := repository.DBFromCtx(ctx, r.db).Save(user).Error
	return errors.WithStack(err)
}
