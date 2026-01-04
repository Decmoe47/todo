package impl

import (
	"context"
	"testing"
	"todo/internal/model/entity"

	"github.com/stretchr/testify/require"
)

func TestTodoRepositoryCRUD(t *testing.T) {
	db := newTestDB(t)
	repo := NewTodoRepository(db)

	list := entity.NewTodoList("L", 1, false)
	require.NoError(t, db.Save(list).Error)

	todo := entity.NewTodo("c", nil, list.ID, 1)
	saved, err := repo.Save(context.Background(), todo)
	require.NoError(t, err)
	require.NotZero(t, saved.ID)

	byID, err := repo.GetByID(context.Background(), saved.ID)
	require.NoError(t, err)
	require.Equal(t, "c", byID.Content)

	listTodos, err := repo.SelectByListID(context.Background(), list.ID)
	require.NoError(t, err)
	require.Len(t, listTodos, 1)

	byIDs, err := repo.SelectByIDs(context.Background(), []uint{saved.ID})
	require.NoError(t, err)
	require.Len(t, byIDs, 1)

	saved.Content = "c2"
	updated, err := repo.Update(context.Background(), saved)
	require.NoError(t, err)
	require.Equal(t, "c2", updated.Content)

	require.NoError(t, repo.SoftDelete(context.Background(), saved.ID))

	require.NoError(t, repo.HardDelete(context.Background(), saved.ID))
}

func TestTodoRepositorySelectByIDsEmpty(t *testing.T) {
	db := newTestDB(t)
	repo := NewTodoRepository(db)

	items, err := repo.SelectByIDs(context.Background(), []uint{})
	require.NoError(t, err)
	require.Empty(t, items)
}

func TestTodoRepositoryDeleteByBelongedListID(t *testing.T) {
	db := newTestDB(t)
	repo := NewTodoRepository(db)

	list := entity.NewTodoList("L", 1, false)
	require.NoError(t, db.Save(list).Error)

	todo := entity.NewTodo("c", nil, list.ID, 1)
	_, err := repo.Save(context.Background(), todo)
	require.NoError(t, err)

	require.NoError(t, repo.DeleteByBelongedListID(context.Background(), list.ID))

	var count int64
	require.NoError(t, db.Model(&entity.Todo{}).Where("belonged_list_id = ?", list.ID).Count(&count).Error)
	require.Equal(t, int64(0), count)
}
