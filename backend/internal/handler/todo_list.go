package handler

import (
	"context"
	"todo/internal/model/request"
	"todo/internal/model/response"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
)

type TodoList struct {
	svc *service.TodoListService
}

func NewTodoListHandler(svc *service.TodoListService) *TodoList {
	return &TodoList{svc: svc}
}

func (h *TodoList) GetCustomTodoLists(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	respond(c, func(ctx context.Context) (any, error) {
		return h.svc.GetCustomTodoLists(ctx, userID)
	})
}

func (h *TodoList) AddTodoList(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoListAdd) (*response.TodoList, error) {
		return h.svc.AddTodoList(ctx, userID, req)
	})
}

func (h *TodoList) UpdateTodoList(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoListUpdate) (*response.TodoList, error) {
		return h.svc.UpdateTodoList(ctx, userID, req)
	})
}

func (h *TodoList) DeleteTodoList(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoListDelete) (any, error) {
		return nil, h.svc.DeleteTodoList(ctx, userID, req)
	})
}
