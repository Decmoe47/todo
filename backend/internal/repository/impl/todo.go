package impl

import (
	"context"
	"todo/internal/model/entity"
	"todo/internal/repository"

	"github.com/cockroachdb/errors"
	"gorm.io/gorm"
)

type TodoRepository struct {
	db *gorm.DB
}

func NewTodoRepository(db *gorm.DB) *TodoRepository {
	return &TodoRepository{db: db}
}

func (r *TodoRepository) SelectByListID(ctx context.Context, listID uint) ([]*entity.Todo, error) {
	db := repository.DBFromCtx(ctx, r.db).
		Preload("BelongedList").
		Preload("CreatedBy").
		Preload("UpdatedBy")
	todos, err := gorm.G[*entity.Todo](db).Where("belonged_list_id = ?", listID).Find(ctx)
	return todos, errors.WithStack(err)
}

func (r *TodoRepository) GetByID(ctx context.Context, id uint) (*entity.Todo, error) {
	db := repository.DBFromCtx(ctx, r.db).
		Preload("BelongedList").
		Preload("CreatedBy").
		Preload("UpdatedBy")
	todo, err := gorm.G[*entity.Todo](db).Where("id = ?", id).First(ctx)
	return todo, errors.WithStack(err)
}

func (r *TodoRepository) SelectByIDs(ctx context.Context, ids []uint) ([]*entity.Todo, error) {
	if len(ids) == 0 {
		return []*entity.Todo{}, nil
	}
	db := repository.DBFromCtx(ctx, r.db).
		Preload("BelongedList").
		Preload("CreatedBy").
		Preload("UpdatedBy")
	todos, err := gorm.G[*entity.Todo](db).Where("id IN ?", ids).Find(ctx)
	return todos, errors.WithStack(err)
}

func (r *TodoRepository) Save(ctx context.Context, todo *entity.Todo) (*entity.Todo, error) {
	err := repository.DBFromCtx(ctx, r.db).Create(todo).Error
	return todo, errors.WithStack(err)
}

func (r *TodoRepository) Update(ctx context.Context, todo *entity.Todo) (*entity.Todo, error) {
	err := repository.DBFromCtx(ctx, r.db).Save(todo).Error
	return todo, errors.WithStack(err)
}

func (r *TodoRepository) SoftDelete(ctx context.Context, id uint) error {
	err := repository.DBFromCtx(ctx, r.db).Delete(&entity.Todo{}, id).Error
	return errors.WithStack(err)
}

func (r *TodoRepository) HardDelete(ctx context.Context, id uint) error {
	err := repository.DBFromCtx(ctx, r.db).Unscoped().Delete(&entity.Todo{}, id).Error
	return errors.WithStack(err)
}

func (r *TodoRepository) DeleteByBelongedListID(ctx context.Context, belongedListID uint) error {
	err := repository.DBFromCtx(ctx, r.db).Unscoped().Where("belonged_list_id = ?", belongedListID).Delete(&entity.Todo{}).Error
	return errors.WithStack(err)
}
