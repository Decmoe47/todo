package handler

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"testing"
	"todo/internal/constant"
	"todo/internal/model/dto"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestTodoListHandlers(t *testing.T) {
	userID := uint(1)
	listRepo := newFakeTodoListRepo()
	todoRepo := newFakeTodoRepo()
	listSvc := service.NewTodoListService(&service.NewTodoListServiceOptions{
		TodoListRepo: listRepo,
		TodoRepo:     todoRepo,
		UserRepo:     &fakeUserRepo{},
		TxMgr:        &fakeTxMgr{},
	})
	h := NewTodoListHandler(listSvc)

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todoLists/add", bytes.NewBufferString(`{"name":"L"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.AddTodoList(c)
	require.Equal(t, http.StatusOK, w.Code)

	listID := uint(1)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todoLists/update", bytes.NewBufferString(`{"id":`+itoa(listID)+`,"name":"N"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.UpdateTodoList(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodGet, "/todoLists/custom", nil)
	h.GetCustomTodoLists(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Set(constant.CtxKeyUser, &dto.User{ID: userID})
	c.Request = httptest.NewRequest(http.MethodPost, "/todoLists/delete", bytes.NewBufferString(`{"id":`+itoa(listID)+`}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.DeleteTodoList(c)
	require.Equal(t, http.StatusOK, w.Code)
}

func TestTodoListAddMissingUser(t *testing.T) {
	listRepo := newFakeTodoListRepo()
	todoRepo := newFakeTodoRepo()
	listSvc := service.NewTodoListService(&service.NewTodoListServiceOptions{
		TodoListRepo: listRepo,
		TodoRepo:     todoRepo,
		UserRepo:     &fakeUserRepo{},
		TxMgr:        &fakeTxMgr{},
	})
	h := NewTodoListHandler(listSvc)

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/todoLists/add", bytes.NewBufferString(`{"name":"L"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.AddTodoList(c)
	require.Len(t, c.Errors, 1)
}
