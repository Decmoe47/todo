package handler

import (
	"context"
	"strconv"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/dto"
	"todo/internal/util"

	"github.com/cockroachdb/errors"
	"github.com/gin-gonic/gin"
)

func getUserIDFromContext(c *gin.Context) (uint, bool) {
	authUser, ok := c.Get(constant.CtxKeyUser)
	if !ok {
		c.Error(errs.NewBusinessErr(respcode.Unauthorized))
		return 0, false
	}

	user, ok := authUser.(*dto.User)
	if !ok || user == nil {
		c.Error(errs.NewInternalServerErr(errors.New("invalid auth user in context"), "Failed to read user from context"))
		return 0, false
	}

	return user.ID, true
}

func parseUserIDParam(c *gin.Context, name string) (uint, bool) {
	idStr := c.Param(name)
	parsed, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		c.Error(errors.WithStack(err))
		return 0, false
	}
	return uint(parsed), true
}

func respond[T any](c *gin.Context, fn func(ctx context.Context) (T, error)) {
	resp, err := fn(c.Request.Context())
	if err != nil {
		c.Error(err)
		return
	}
	util.ResponseOk(c, resp)
}

func action(c *gin.Context, fn func(ctx context.Context) error) {
	if err := fn(c.Request.Context()); err != nil {
		c.Error(err)
		return
	}
	util.ResponseOk(c, nil)
}

func bindJsonThenRespond[TReq any, TResp any](c *gin.Context, fn func(ctx context.Context, req *TReq) (TResp, error)) {
	var req TReq
	if err := c.ShouldBindBodyWithJSON(&req); err != nil {
		c.Error(err)
		return
	}
	resp, err := fn(c.Request.Context(), &req)
	if err != nil {
		c.Error(err)
		return
	}
	util.ResponseOk(c, resp)
}

func bindJsonThenAction[TReq any](c *gin.Context, fn func(ctx context.Context, req *TReq) error) {
	var req TReq
	if err := c.ShouldBindBodyWithJSON(&req); err != nil {
		c.Error(err)
		return
	}
	if err := fn(c.Request.Context(), &req); err != nil {
		c.Error(err)
		return
	}
	util.ResponseOk(c, nil)
}
