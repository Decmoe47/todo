package handler

import (
	"bytes"
	"context"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"
	"todo/internal/config"
	"todo/internal/constant"
	"todo/internal/model/entity"
	"todo/internal/model/request"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

func TestAuthHandlers(t *testing.T) {
	deps := newTestDeps(t)
	defer deps.close()

	password, err := bcrypt.GenerateFromPassword([]byte("pass"), bcrypt.DefaultCost)
	require.NoError(t, err)
	user := &entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{}}, Email: "a@b.com", Name: "A", Password: string(password)}
	require.NoError(t, deps.db.Save(user).Error)

	addr, stop := startSMTPServer(t)
	defer stop()
	parts := strings.Split(addr, ":")
	mailSvc := service.NewMail(&config.Mail{Host: parts[0], Port: mustAtoi(t, parts[1]), Username: "u", Password: "p", From: "from@test"})

	authSvc := newAuthSvc(t, deps, mailSvc)
	h := NewAuthHandler(authSvc)

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/login", bytes.NewBufferString(`{"email":"a@b.com","password":"pass"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.Login(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	require.NoError(t, deps.redis.Set(context.Background(), constant.RedisKeyVerifyCode+"new@b.com", "1234", time.Minute).Err())
	c.Request = httptest.NewRequest(http.MethodPost, "/register", bytes.NewBufferString(`{"email":"new@b.com","password":"pass","name":"N","verifyCode":"1234"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.Register(c)
	require.Equal(t, http.StatusOK, w.Code)

	tokens, err := authSvc.Login(context.Background(), &request.UserLogin{Email: "a@b.com", Password: "pass"})
	require.NoError(t, err)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/refresh", bytes.NewBufferString(`{"refresh_token":"`+tokens.AuthTokens.RefreshToken+`"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.RefreshToken(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/logout", nil)
	c.Request.Header.Set("Authorization", tokens.AuthTokens.AccessToken)
	h.Logout(c)
	require.Equal(t, http.StatusOK, w.Code)

	w = httptest.NewRecorder()
	c, _ = gin.CreateTestContext(w)
	c.Request = httptest.NewRequest(http.MethodPost, "/send-verify", bytes.NewBufferString(`{"email":"v@b.com"}`))
	c.Request.Header.Set("Content-Type", "application/json")
	h.SendVerifyCode(c)
	require.Equal(t, http.StatusOK, w.Code)
}
