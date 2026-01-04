package impl

import (
	"context"
	"testing"
	"todo/internal/model/entity"

	"github.com/stretchr/testify/require"
)

func TestUserRepositoryCRUD(t *testing.T) {
	db := newTestDB(t)
	repo := NewUserRepository(db)

	user := &entity.User{Email: "a@b.com", Name: "A", Password: "p"}
	require.NoError(t, repo.Save(context.Background(), user))
	require.NotZero(t, user.ID)

	byID, err := repo.GetByID(context.Background(), user.ID)
	require.NoError(t, err)
	require.Equal(t, user.Email, byID.Email)

	byEmail, err := repo.GetByEmail(context.Background(), "a@b.com")
	require.NoError(t, err)
	require.Equal(t, user.ID, byEmail.ID)

	has, err := repo.HasByEmail(context.Background(), "a@b.com")
	require.NoError(t, err)
	require.True(t, has)

	list, err := repo.SelectByEmail(context.Background(), "a@b.com")
	require.NoError(t, err)
	require.Len(t, list, 1)

	byName, err := repo.SelectByName(context.Background(), "A")
	require.NoError(t, err)
	require.Len(t, byName, 1)
}
