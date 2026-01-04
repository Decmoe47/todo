package handler

import (
	"context"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/request"
	"todo/internal/model/response"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
)

type Todo struct {
	svc *service.TodoService
}

func NewTodoHandler(svc *service.TodoService) *Todo {
	return &Todo{svc: svc}
}

func (h *Todo) GetTodos(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}

	listID := c.Query("listId")
	if listID == "" {
		c.Error(errs.NewBusinessErr(respcode.InvalidParam))
		return
	}

	respond(c, func(ctx context.Context) ([]*response.Todo, error) {
		return h.svc.GetTodos(ctx, userID, listID)
	})
}

func (h *Todo) AddTodo(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoAdd) (*response.Todo, error) {
		return h.svc.AddTodo(ctx, userID, req)
	})
}

func (h *Todo) DeleteTodo(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoDelete) (any, error) {
		return nil, h.svc.DeleteTodo(ctx, userID, req)
	})
}

func (h *Todo) UpdateTodo(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoUpdate) (*response.Todo, error) {
		return h.svc.UpdateTodo(ctx, userID, req)
	})
}

func (h *Todo) ToggleTodo(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoToggle) (*response.Todo, error) {
		return h.svc.ToggleTodo(ctx, userID, req)
	})
}

func (h *Todo) MoveTodo(c *gin.Context) {
	userID, ok := getUserIDFromContext(c)
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.TodoMove) (*response.Todo, error) {
		return h.svc.MoveTodo(ctx, userID, req)
	})
}
