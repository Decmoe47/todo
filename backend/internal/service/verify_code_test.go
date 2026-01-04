package service

import (
	"context"
	"testing"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"

	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/require"
)

func TestVerifyCodeCreateAndGet(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	client := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	svc, err := NewVerifyCodeService(client)
	require.NoError(t, err)

	code, err := svc.CreateCode(context.Background(), "a@b.com")
	require.NoError(t, err)
	require.Len(t, code, 4)

	stored, err := mr.Get(constant.RedisKeyVerifyCode + "a@b.com")
	require.NoError(t, err)
	require.Equal(t, code, stored)

	got, err := svc.GetCode(context.Background(), "a@b.com")
	require.NoError(t, err)
	require.Equal(t, code, got)
}

func TestVerifyCodeGetMissing(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	client := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	svc, err := NewVerifyCodeService(client)
	require.NoError(t, err)

	_, err = svc.GetCode(context.Background(), "missing@b.com")
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.VerificationCodeExpired, be.Code)
}

func TestVerifyCodeCheckMismatch(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	client := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	svc, err := NewVerifyCodeService(client)
	require.NoError(t, err)

	require.NoError(t, client.Set(context.Background(), constant.RedisKeyVerifyCode+"a@b.com", "1234", 0).Err())
	err = svc.CheckCode(context.Background(), "9999", "a@b.com")
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.VerificationCodeIncorrect, be.Code)
}

func TestVerifyCodeAssertSameCode(t *testing.T) {
	svc := &VerifyCodeService{}
	require.NoError(t, svc.AssertSameCode("1234", "1234"))
}
