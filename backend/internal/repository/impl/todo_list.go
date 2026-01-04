package impl

import (
	"context"
	"todo/internal/model/entity"
	"todo/internal/repository"

	"github.com/cockroachdb/errors"
	"gorm.io/gorm"
)

type TodoListRepository struct {
	db *gorm.DB
}

func NewTodoListRepository(db *gorm.DB) *TodoListRepository {
	return &TodoListRepository{db: db}
}

func (r *TodoListRepository) SelectExcludingInbox(ctx context.Context, userID uint) ([]*entity.TodoList, error) {
	db := repository.DBFromCtx(ctx, r.db).
		Preload("CreatedBy").
		Preload("UpdatedBy")
	lists, err := gorm.G[*entity.TodoList](db).
		Where("created_by = ? AND inbox = ?", userID, false).
		Find(ctx)
	return lists, errors.WithStack(err)
}

func (r *TodoListRepository) GetInbox(ctx context.Context, userID uint) (*entity.TodoList, error) {
	db := repository.DBFromCtx(ctx, r.db).
		Preload("CreatedBy").
		Preload("UpdatedBy")
	list, err := gorm.G[*entity.TodoList](db).
		Where("created_by = ? AND inbox = ?", userID, true).
		First(ctx)
	return list, errors.WithStack(err)
}

func (r *TodoListRepository) GetByID(ctx context.Context, id uint) (*entity.TodoList, error) {
	db := repository.DBFromCtx(ctx, r.db).
		Preload("CreatedBy").
		Preload("UpdatedBy")
	list, err := gorm.G[*entity.TodoList](db).Where("id = ?", id).First(ctx)
	return list, errors.WithStack(err)
}

func (r *TodoListRepository) Save(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error) {
	err := repository.DBFromCtx(ctx, r.db).Create(todoList).Error
	return todoList, errors.WithStack(err)
}

func (r *TodoListRepository) Update(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error) {
	err := repository.DBFromCtx(ctx, r.db).Save(todoList).Error
	return todoList, errors.WithStack(err)
}

func (r *TodoListRepository) Delete(ctx context.Context, id uint) error {
	err := repository.DBFromCtx(ctx, r.db).Unscoped().Delete(&entity.TodoList{}, id).Error
	return errors.WithStack(err)
}
