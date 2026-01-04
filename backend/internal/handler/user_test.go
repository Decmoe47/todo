package handler

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"testing"
	"todo/internal/model/dto"
	"todo/internal/model/entity"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func TestUserHandlers(t *testing.T) {
	deps := newTestDeps(t)
	defer deps.close()

	user := &entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{}}, Email: "a@b.com", Name: "A"}
	require.NoError(t, deps.db.Save(user).Error)

	userSvc := newUserSvc(t, deps)
	h := NewUserHandler(userSvc)

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Params = gin.Params{gin.Param{Key: "userId", Value: itoa(user.ID)}}
	c.Request = httptest.NewRequest(http.MethodGet, "/"+itoa(user.ID), nil)
	h.GetUser(c)
	require.Equal(t, http.StatusOK, w.Code)

	tokens, err := newJwtSvc(t, deps.redis).Generate(&dto.User{ID: user.ID, Email: user.Email, Name: user.Name})
	require.NoError(t, err)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/by-token?token="+tokens.AccessToken, nil)
	h.GetUserByToken(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/search", bytes.NewBufferString(`{"name":"A"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.SearchUser(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Params = gin.Params{gin.Param{Key: "userId", Value: itoa(user.ID)}}
	c.Request = httptest.NewRequest(http.MethodPost, "/update", bytes.NewBufferString(`{"name":"B"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.UpdateUser(c)
	require.Equal(t, http.StatusOK, w.Code)
}

func TestUserGetUserInvalidParam(t *testing.T) {
	deps := newTestDeps(t)
	defer deps.close()
	userSvc := newUserSvc(t, deps)
	h := NewUserHandler(userSvc)

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Params = gin.Params{gin.Param{Key: "userId", Value: "bad"}}
	c.Request = httptest.NewRequest(http.MethodGet, "/bad", nil)
	h.GetUser(c)
	require.Len(t, c.Errors, 1)
}
