package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"todo/internal/config"
	"todo/internal/constant"
	"todo/internal/model/dto"
	"todo/internal/service"

	"github.com/alicebob/miniredis/v2"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/require"
)

func TestJwtAuthWhitelist(t *testing.T) {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(JwtAuth(&service.JwtService{}))
	r.GET("/api/auth/login", func(c *gin.Context) { c.Status(http.StatusOK) })

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/auth/login", nil)
	r.ServeHTTP(w, req)

	require.Equal(t, http.StatusOK, w.Code)
}

func TestJwtAuthMissingToken(t *testing.T) {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(JwtAuth(&service.JwtService{}))
	r.GET("/api/todos", func(c *gin.Context) { c.Status(http.StatusOK) })

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/todos", nil)
	r.ServeHTTP(w, req)

	require.Equal(t, http.StatusUnauthorized, w.Code)
}

func TestJwtAuthInvalidPrefix(t *testing.T) {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(JwtAuth(&service.JwtService{}))
	r.GET("/api/todos", func(c *gin.Context) { c.Status(http.StatusOK) })

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/todos", nil)
	req.Header.Set("Authorization", "Token abc")
	r.ServeHTTP(w, req)

	require.Equal(t, http.StatusUnauthorized, w.Code)
}

func TestJwtAuthValidToken(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	rdb := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	jwtSvc, err := service.NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)
	tokens, err := jwtSvc.Generate(&dto.User{ID: 1, Email: "a@b.com", Name: "A"})
	require.NoError(t, err)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(JwtAuth(jwtSvc))
	r.GET("/api/todos", func(c *gin.Context) {
		_, ok := c.Get(constant.CtxKeyUser)
		if ok {
			c.Status(http.StatusOK)
			return
		}
		c.Status(http.StatusUnauthorized)
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/todos", nil)
	req.Header.Set("Authorization", "Bearer "+tokens.AccessToken)
	r.ServeHTTP(w, req)

	require.Equal(t, http.StatusOK, w.Code)
}

func TestJwtAuthInvalidToken(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	rdb := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	jwtSvc, err := service.NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(JwtAuth(jwtSvc))
	r.GET("/api/todos", func(c *gin.Context) { c.Status(http.StatusOK) })

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/todos", nil)
	req.Header.Set("Authorization", "Bearer bad.token")
	r.ServeHTTP(w, req)

	require.Equal(t, http.StatusUnauthorized, w.Code)
}
