package util

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"todo/internal/constant/enum/respcode"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestResponseOk(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	ResponseOk(c, map[string]string{"ok": "yes"})

	require.Equal(t, http.StatusOK, w.Code)
	var out map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &out))
	require.Equal(t, map[string]any{"ok": "yes"}, out["data"])
	require.NotNil(t, out["code"])
}

func TestResponseInternalServerErr(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	ResponseInternalServerErr(c, respcode.InternalServerError)

	require.Equal(t, http.StatusInternalServerError, w.Code)
	var out map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &out))
	require.Equal(t, respcode.InternalServerError.Message(), out["message"])
	require.NotNil(t, out["code"])
}

func TestResponseInternalServerErrWithData(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	ResponseInternalServerErrWithData(c, respcode.InvalidParam, map[string]string{"err": "bad"})

	require.Equal(t, http.StatusInternalServerError, w.Code)
	var out map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &out))
	require.Equal(t, respcode.InvalidParam.Message(), out["message"])
	require.Equal(t, map[string]any{"err": "bad"}, out["data"])
	require.NotNil(t, out["code"])
}

func TestResponseUnauthorized(t *testing.T) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	ResponseUnauthorized(c, respcode.Unauthorized)

	require.Equal(t, http.StatusUnauthorized, w.Code)
	var out map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &out))
	require.Equal(t, respcode.Unauthorized.Message(), out["message"])
	require.NotNil(t, out["code"])
}
