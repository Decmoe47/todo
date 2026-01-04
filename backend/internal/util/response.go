package util

import (
	"net/http"
	"todo/internal/constant/enum/respcode"

	"github.com/gin-gonic/gin"
)

type R struct {
	Code    *respcode.RespCode `json:"code"`
	Message string             `json:"message"`
	Data    any                `json:"data"`
}

func ResponseOk(c *gin.Context, data any) {
	c.JSON(http.StatusOK, &R{
		Code: respcode.Ok,
		Data: data,
	})
}

func ResponseInternalServerErr(c *gin.Context, code *respcode.RespCode) {
	c.JSON(http.StatusInternalServerError, &R{
		Code:    code,
		Message: code.Message(),
	})
}

func ResponseInternalServerErrWithData(c *gin.Context, code *respcode.RespCode, data any) {
	c.JSON(http.StatusInternalServerError, &R{
		Code:    code,
		Message: code.Message(),
		Data:    data,
	})
}

func ResponseUnauthorized(c *gin.Context, code *respcode.RespCode) {
	c.JSON(http.StatusUnauthorized, &R{
		Code:    code,
		Message: code.Message(),
	})
}
