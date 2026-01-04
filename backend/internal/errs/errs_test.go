package errs

import (
	"errors"
	"testing"
	"todo/internal/constant/enum/respcode"

	"github.com/stretchr/testify/require"
)

func TestBusinessErrErrorNilReceiver(t *testing.T) {
	var err *BusinessErr
	require.Equal(t, "", err.Error())
}

func TestNewBusinessErr(t *testing.T) {
	err := NewBusinessErr(respcode.InvalidParam)
	var be *BusinessErr
	require.True(t, errors.As(err, &be))
	require.Equal(t, respcode.InvalidParam, be.Code)
	require.Contains(t, err.Error(), "code: 10003")
}

func TestNewBusinessErrWithAdditionalMsg(t *testing.T) {
	err := NewBusinessErrWithAdditionalMsg(respcode.PermissionDenied, "deny %s", "me")
	var be *BusinessErr
	require.True(t, errors.As(err, &be))
	require.Equal(t, respcode.PermissionDenied, be.Code)
	require.Equal(t, "deny me", be.AdditionalMsg)
	require.Contains(t, err.Error(), "additionalMessage")
}

func TestNewBusinessErrWithCause(t *testing.T) {
	cause := errors.New("root")
	err := NewBusinessErrWithCause(respcode.UserNotFound, cause, "missing")
	var be *BusinessErr
	require.True(t, errors.As(err, &be))
	require.Equal(t, respcode.UserNotFound, be.Code)
	require.Equal(t, cause, be.Cause)
	require.Contains(t, err.Error(), "missing")
}

func TestNewInternalServerErr(t *testing.T) {
	cause := errors.New("boom")
	err := NewInternalServerErr(cause, "failed %d", 1)
	var be *BusinessErr
	require.True(t, errors.As(err, &be))
	require.Equal(t, respcode.InternalServerError, be.Code)
	require.Equal(t, cause, be.Cause)
	require.Contains(t, err.Error(), "failed 1")
}
