package middleware

import (
	"log/slog"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/util"

	"github.com/cockroachdb/errors"
	"github.com/gin-gonic/gin"
)

func GlobalErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()

		if len(c.Errors) > 0 {
			err := c.Errors.Last().Err
			slog.Error("Error is caught in global error handler", "error", err)

			var businessErr *errs.BusinessErr
			if errors.As(err, &businessErr) {
				util.ResponseInternalServerErr(c, businessErr.Code)
			} else {
				util.ResponseInternalServerErr(c, respcode.InternalServerError)
			}
		}
	}
}
