package handler

import (
	"bytes"
	"context"
	"net/http"
	"net/http/httptest"
	"testing"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/dto"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestGetUserIDFromContext(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodGet, "/", nil)

	c.Set(constant.CtxKeyUser, &dto.User{ID: 7})
	id, ok := getUserIDFromContext(c)
	require.True(t, ok)
	require.Equal(t, uint(7), id)
}

func TestGetUserIDFromContextMissing(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", nil)

	_, ok := getUserIDFromContext(c)
	require.False(t, ok)
	require.Len(t, c.Errors, 1)
	var be *errs.BusinessErr
	require.ErrorAs(t, c.Errors.Last().Err, &be)
	require.Equal(t, respcode.Unauthorized, be.Code)
}

func TestParseUserIDParam(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodGet, "/", nil)
	c.Params = gin.Params{gin.Param{Key: "userId", Value: "12"}}

	id, ok := parseUserIDParam(c, "userId")
	require.True(t, ok)
	require.Equal(t, uint(12), id)
}

func TestRespond(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodGet, "/", nil)
	c.Request = httptest.NewRequest(http.MethodPost, "/", nil)

	respond(c, func(ctx context.Context) (string, error) { return "ok", nil })
	require.Equal(t, http.StatusOK, w.Code)
}

func TestAction(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", nil)

	action(c, func(ctx context.Context) error { return nil })
	require.Equal(t, http.StatusOK, w.Code)
}

func TestBindJsonThenRespond(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", bytes.NewBufferString(`{"name":"a"}`))
	c.Request.Header.Set("Content-Type", "application/json")

	type req struct{ Name string }
	bindJsonThenRespond(c, func(ctx context.Context, r *req) (string, error) { return r.Name, nil })
	require.Equal(t, http.StatusOK, w.Code)
}

func TestBindJsonThenAction(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", bytes.NewBufferString(`{"name":"a"}`))
	c.Request.Header.Set("Content-Type", "application/json")

	type req struct{ Name string }
	bindJsonThenAction(c, func(ctx context.Context, r *req) error { return nil })
	require.Equal(t, http.StatusOK, w.Code)
}

func TestGetUserIDFromContextInvalidType(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodGet, "/", nil)
	c.Set(constant.CtxKeyUser, "bad")

	_, ok := getUserIDFromContext(c)
	require.False(t, ok)
	require.Len(t, c.Errors, 1)
}

func TestParseUserIDParamInvalid(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Params = gin.Params{gin.Param{Key: "userId", Value: "bad"}}

	_, ok := parseUserIDParam(c, "userId")
	require.False(t, ok)
	require.Len(t, c.Errors, 1)
}

func TestRespondError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodGet, "/", nil)

	respond(c, func(ctx context.Context) (string, error) { return "", errs.ErrVerifyToken })
	require.Len(t, c.Errors, 1)
}

func TestActionError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", nil)

	action(c, func(ctx context.Context) error { return errs.ErrVerifyToken })
	require.Len(t, c.Errors, 1)
}

func TestBindJsonThenRespondError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", bytes.NewBufferString(`{bad}`))
	c.Request.Header.Set("Content-Type", "application/json")

	type req struct{ Name string }
	bindJsonThenRespond(c, func(ctx context.Context, r *req) (string, error) { return r.Name, nil })
	require.Len(t, c.Errors, 1)
}

func TestBindJsonThenActionError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", bytes.NewBufferString(`{bad}`))
	c.Request.Header.Set("Content-Type", "application/json")

	type req struct{ Name string }
	bindJsonThenAction(c, func(ctx context.Context, r *req) error { return nil })
	require.Len(t, c.Errors, 1)
}

func TestBindJsonThenRespondActionError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", bytes.NewBufferString(`{"name":"a"}`))
	c.Request.Header.Set("Content-Type", "application/json")

	type req struct{ Name string }
	bindJsonThenRespond(c, func(ctx context.Context, r *req) (string, error) { return "", errs.ErrVerifyToken })
	require.Len(t, c.Errors, 1)
}

func TestBindJsonThenActionActionError(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/", bytes.NewBufferString(`{"name":"a"}`))
	c.Request.Header.Set("Content-Type", "application/json")

	type req struct{ Name string }
	bindJsonThenAction(c, func(ctx context.Context, r *req) error { return errs.ErrVerifyToken })
	require.Len(t, c.Errors, 1)
}
