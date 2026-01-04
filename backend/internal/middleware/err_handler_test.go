package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestGlobalErrorHandlerBusinessErr(t *testing.T) {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(GlobalErrorHandler())
	r.GET("/", func(c *gin.Context) {
		c.Error(errs.NewBusinessErr(respcode.InvalidParam))
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	r.ServeHTTP(w, req)

	require.Equal(t, http.StatusInternalServerError, w.Code)
}

func TestGlobalErrorHandlerNonBusinessErr(t *testing.T) {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(GlobalErrorHandler())
	r.GET("/", func(c *gin.Context) {
		c.Error(errs.ErrVerifyToken)
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	r.ServeHTTP(w, req)

	require.Equal(t, http.StatusInternalServerError, w.Code)
}
