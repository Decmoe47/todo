package service

import (
	"context"
	"crypto/rand"
	"math/big"
	"time"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"

	"github.com/redis/go-redis/v9"
)

type VerifyCodeService struct {
	redis *redis.Client
}

func NewVerifyCodeService(redis *redis.Client) (*VerifyCodeService, error) {
	return &VerifyCodeService{
		redis: redis,
	}, nil
}

// CreateCode 生成4位数字验证码并保存到 Redis
func (s *VerifyCodeService) CreateCode(ctx context.Context, email string) (string, error) {
	code, err := randomDigits(4)
	if err != nil {
		return "", errs.NewInternalServerErr(err, "Failed to create code for %s", email)
	}

	err = s.redis.Set(ctx, constant.RedisKeyVerifyCode+email, code, time.Minute*5).Err()
	if err != nil {
		return "", errs.NewInternalServerErr(err, "Failed to create code for %s", email)
	}
	return code, nil
}

// GetCode 从 Redis 读取验证码，如果不存在返回错误
func (s *VerifyCodeService) GetCode(ctx context.Context, email string) (string, error) {
	v, err := s.redis.Get(ctx, constant.RedisKeyVerifyCode+email).Result()
	if err != nil {
		return "", errs.NewBusinessErrWithCause(respcode.VerificationCodeExpired, err, "Failed to get code for %s", email)
	}
	return v, nil
}

// AssertSameCode 比较两个验证码（允许为空指针），不相等返回错误
func (s *VerifyCodeService) AssertSameCode(source string, target string) error {
	if source != target {
		return errs.NewBusinessErr(respcode.VerificationCodeIncorrect)
	}
	return nil
}

// CheckCode 从 Redis 获取并比较
func (s *VerifyCodeService) CheckCode(ctx context.Context, code string, email string) error {
	src, err := s.GetCode(ctx, email)
	if err != nil {
		return err
	}
	return s.AssertSameCode(src, code)
}

func randomDigits(n int) (string, error) {
	result := make([]byte, n)
	for i := 0; i < n; i++ {
		// [0,9]
		num, err := rand.Int(rand.Reader, big.NewInt(10))
		if err != nil {
			return "", errs.NewInternalServerErr(err, "Failed to generate random number")
		}
		result[i] = byte('0') + byte(num.Int64())
	}
	return string(result), nil
}
