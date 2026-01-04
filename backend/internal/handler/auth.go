package handler

import (
	"context"
	"todo/internal/model/request"
	"todo/internal/model/response"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
)

type Auth struct {
	svc *service.AuthService
}

func NewAuthHandler(svc *service.AuthService) *Auth {
	return &Auth{svc: svc}
}

func (h *Auth) Login(c *gin.Context) {
	bindJsonThenRespond(c, h.svc.Login)
}

func (h *Auth) Register(c *gin.Context) {
	bindJsonThenRespond(c, h.svc.Register)
}

func (h *Auth) RefreshToken(c *gin.Context) {
	bindJsonThenRespond(c, func(_ context.Context, req *request.RefreshTokenReq) (*response.AuthTokens, error) {
		return h.svc.RefreshAccessToken(req)
	})
}

func (h *Auth) Logout(c *gin.Context) {
	action(c, func(_ context.Context) error {
		return h.svc.Logout(c.GetHeader("Authorization"))
	})
}

func (h *Auth) SendVerifyCode(c *gin.Context) {
	bindJsonThenAction(c, h.svc.SendVerifyCode)
}
