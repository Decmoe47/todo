package entity

import (
	"testing"
	"time"

	"github.com/stretchr/testify/require"
)

func TestEntityConstructors(t *testing.T) {
	due := time.Now()
	todo := NewTodo("c", &due, 3, 1)
	require.Equal(t, "c", todo.Content)
	require.Equal(t, uint(3), todo.BelongedListID)
	require.Equal(t, uint(1), todo.CreatedByID)
	require.False(t, todo.Done)

	list := NewTodoList("L", 2, true)
	require.Equal(t, "L", list.Name)
	require.True(t, list.Inbox)
	require.Equal(t, uint(2), list.CreatedByID)

	user := NewUser("a@b.com", "p", "A")
	require.Equal(t, "a@b.com", user.Email)

	u2 := NewUserWithIdAndEmail(5, "b@b.com")
	require.Equal(t, uint(5), u2.ID)
	require.Equal(t, "b@b.com", u2.Email)
}
