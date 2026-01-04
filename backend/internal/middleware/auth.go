package middleware

import (
	"log/slog"
	"strings"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/service"
	"todo/internal/util"

	"github.com/cockroachdb/errors"
	"github.com/gin-gonic/gin"
	"github.com/samber/lo"
)

func JwtAuth(jwtSvc *service.JwtService) gin.HandlerFunc {
	return func(c *gin.Context) {
		if lo.Contains(constant.AuthWhiteList, c.Request.URL.Path) {
			c.Next()
			return
		}

		auth := c.GetHeader("Authorization")
		if auth == "" || !strings.HasPrefix(auth, "Bearer ") {
			slog.Error("missing bearer token")
			util.ResponseUnauthorized(c, respcode.Unauthorized)
			return
		}
		tokenStr := strings.TrimPrefix(auth, "Bearer ")

		if valid := jwtSvc.IsValid(tokenStr); !valid {
			util.ResponseUnauthorized(c, respcode.Unauthorized)
			return
		}
		user, err := jwtSvc.Parse(tokenStr)
		if err != nil {
			var businessErr *errs.BusinessErr
			if errors.As(err, &businessErr) {
				util.ResponseUnauthorized(c, businessErr.Code)
			} else {
				util.ResponseUnauthorized(c, respcode.Unauthorized)
			}
			return
		}
		c.Set(constant.CtxKeyUser, user)

		c.Next()
	}
}
