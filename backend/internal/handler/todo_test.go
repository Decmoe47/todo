package handler

import (
	"bytes"
	"context"
	"net/http"
	"net/http/httptest"
	"testing"
	"todo/internal/constant"
	"todo/internal/model/dto"
	"todo/internal/model/entity"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestTodoHandlers(t *testing.T) {
	userID := uint(1)
	listRepo := newFakeTodoListRepo()
	todoRepo := newFakeTodoRepo()
	list, err := listRepo.Save(context.Background(), entity.NewTodoList("L", userID, false))
	require.NoError(t, err)

	todoSvc := service.NewTodoService(&service.NewTodoServiceOptions{
		TodoRepo:     todoRepo,
		TodoListRepo: listRepo,
		UserRepo:     &fakeUserRepo{},
	})
	h := NewTodoHandler(todoSvc)

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todos/add", bytes.NewBufferString(`{"content":"c","belongedListId":"`+itoa(list.ID)+`"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.AddTodo(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodGet, "/todos?listId="+itoa(list.ID), nil)
	h.GetTodos(c)
	require.Equal(t, http.StatusOK, w.Code)

	todoID := uint(1)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todos/update", bytes.NewBufferString(`{"id":`+itoa(todoID)+`,"content":"c2","done":true}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.UpdateTodo(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todos/toggle", bytes.NewBufferString(`{"id":`+itoa(todoID)+`}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.ToggleTodo(c)
	require.Equal(t, http.StatusOK, w.Code)

	list2, err := listRepo.Save(context.Background(), entity.NewTodoList("L2", userID, false))
	require.NoError(t, err)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todos/move", bytes.NewBufferString(`{"id":`+itoa(todoID)+`,"targetListId":"`+itoa(list2.ID)+`"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.MoveTodo(c)
	require.Equal(t, http.StatusOK, w.Code)

	soft := true
	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todos/delete", bytes.NewBufferString(`{"id":`+itoa(todoID)+`,"softDeleted":`+boolToString(soft)+`}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.DeleteTodo(c)
	require.Equal(t, http.StatusOK, w.Code)
}

func TestTodoGetTodosMissingListID(t *testing.T) {
	userID := uint(1)
	todoSvc := service.NewTodoService(&service.NewTodoServiceOptions{
		TodoRepo:     newFakeTodoRepo(),
		TodoListRepo: newFakeTodoListRepo(),
		UserRepo:     &fakeUserRepo{},
	})
	h := NewTodoHandler(todoSvc)

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodGet, "/todos", nil)
	h.GetTodos(c)
	require.Len(t, c.Errors, 1)
}

func itoa(v uint) string {
	if v == 0 {
		return "0"
	}
	b := make([]byte, 0, 10)
	for v > 0 {
		d := v % 10
		b = append([]byte{byte('0' + d)}, b...)
		v /= 10
	}
	return string(b)
}

func boolToString(v bool) string {
	if v {
		return "true"
	}
	return "false"
}
