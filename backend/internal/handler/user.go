package handler

import (
	"context"
	"todo/internal/model/request"
	"todo/internal/model/response"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
)

type User struct {
	svc *service.UserService
}

func NewUserHandler(svc *service.UserService) *User {
	return &User{svc: svc}
}

func (h *User) GetUser(c *gin.Context) {
	userID, ok := parseUserIDParam(c, "userId")
	if !ok {
		return
	}
	respond(c, func(cc context.Context) (*response.User, error) {
		return h.svc.GetUser(cc, userID)
	})
}

func (h *User) GetUserByToken(c *gin.Context) {
	respond(c, func(ctx context.Context) (*response.User, error) {
		return h.svc.GetUserByToken(ctx, c.Query("token"))
	})
}

func (h *User) SearchUser(c *gin.Context) {
	bindJsonThenRespond(c, func(ctx context.Context, req *request.UserSearch) ([]*response.User, error) {
		return h.svc.SearchUser(ctx, req)
	})
}

func (h *User) UpdateUser(c *gin.Context) {
	userID, ok := parseUserIDParam(c, "userId")
	if !ok {
		return
	}
	bindJsonThenRespond(c, func(ctx context.Context, req *request.UserUpdate) (*response.User, error) {
		return h.svc.UpdateUser(ctx, userID, req)
	})
}
