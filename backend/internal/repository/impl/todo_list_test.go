package impl

import (
	"context"
	"testing"
	"todo/internal/model/entity"

	"github.com/stretchr/testify/require"
)

func TestTodoListRepositoryCRUD(t *testing.T) {
	db := newTestDB(t)
	repo := NewTodoListRepository(db)

	list := entity.NewTodoList("L", 1, false)
	saved, err := repo.Save(context.Background(), list)
	require.NoError(t, err)
	require.NotZero(t, saved.ID)

	byID, err := repo.GetByID(context.Background(), saved.ID)
	require.NoError(t, err)
	require.Equal(t, "L", byID.Name)

	custom, err := repo.SelectExcludingInbox(context.Background(), 1)
	require.NoError(t, err)
	require.Len(t, custom, 1)

	inbox := entity.NewTodoList("Inbox", 1, true)
	_, err = repo.Save(context.Background(), inbox)
	require.NoError(t, err)

	inboxFetched, err := repo.GetInbox(context.Background(), 1)
	require.NoError(t, err)
	require.True(t, inboxFetched.Inbox)

	saved.Name = "N"
	updated, err := repo.Update(context.Background(), saved)
	require.NoError(t, err)
	require.Equal(t, "N", updated.Name)

	require.NoError(t, repo.Delete(context.Background(), saved.ID))
}
