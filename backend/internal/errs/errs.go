package errs

import (
	"fmt"
	"todo/internal/constant/enum/respcode"

	"github.com/cockroachdb/errors"
)

var (
	ErrVerifyToken error = errors.New("Failed to verify token")
)

type BusinessErr struct {
	Code          *respcode.RespCode `json:"code"`
	AdditionalMsg string             `json:"additionalMsg"`
	Cause         error              `json:"cause"`
}

func NewBusinessErr(code *respcode.RespCode) error {
	return errors.WithStack(&BusinessErr{
		Code: code,
	})
}

func NewBusinessErrWithAdditionalMsg(code *respcode.RespCode, additionalMsg string, args ...any) error {
	return errors.WithStack(&BusinessErr{
		Code:          code,
		AdditionalMsg: fmt.Sprintf(additionalMsg, args...),
	})
}

func NewBusinessErrWithCause(code *respcode.RespCode, cause error, additionalMsg string, args ...any) error {
	return errors.WithStack(&BusinessErr{
		Code:          code,
		AdditionalMsg: fmt.Sprintf(additionalMsg, args...),
		Cause:         cause,
	})
}

func NewInternalServerErr(cause error, additionalMsg string, args ...any) error {
	return errors.WithStack(&BusinessErr{
		Code:          respcode.InternalServerError,
		AdditionalMsg: fmt.Sprintf(additionalMsg, args...),
		Cause:         cause,
	})
}

func (e *BusinessErr) Error() string {
	if e == nil {
		return ""
	}
	cause := "<nil>"
	if e.Cause != nil {
		cause = e.Cause.Error()
	}
	if e.AdditionalMsg == "" {
		return fmt.Sprintf("code: %d | message: %s | cause: %s", e.Code.Code(), e.Code.Message(), cause)
	}
	return fmt.Sprintf("code: %d | message: %s | additionalMessage: %s | cause: %s", e.Code.Code(), e.Code.Message(), e.AdditionalMsg, cause)
}
