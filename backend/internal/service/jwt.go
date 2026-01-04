package service

import (
	"context"
	"log/slog"
	"strings"
	"time"
	"todo/internal/config"
	"todo/internal/constant"
	"todo/internal/errs"
	"todo/internal/model/dto"
	"todo/internal/model/response"

	"github.com/cockroachdb/errors"
	"github.com/google/uuid"
	"github.com/kataras/jwt"
	"github.com/redis/go-redis/v9"
)

const errMsgPraseToken = "Failed to parse token: %s"

type JwtService struct {
	securityCfg *config.Security
	redis       *redis.Client
}

func NewJwtService(securityCfg *config.Security, redis *redis.Client) (*JwtService, error) {
	return &JwtService{
		securityCfg: securityCfg,
		redis:       redis,
	}, nil
}

func (s *JwtService) Generate(user *dto.User) (*response.AuthTokens, error) {
	access, err := s.generateToken(user, s.securityCfg.AccessTokenTTL)
	if err != nil {
		return nil, err
	}
	refresh, err := s.generateToken(user, s.securityCfg.RefreshTokenTTL)
	if err != nil {
		return nil, err
	}
	return &response.AuthTokens{AccessToken: access, RefreshToken: refresh}, nil
}

func (s *JwtService) Parse(tokenStr string) (*dto.User, error) {
	tokenStr = trimPrefix(tokenStr)
	claims, err := parseToken(tokenStr, s.securityCfg.Secret)
	if err != nil {
		return nil, err
	}
	return &dto.User{
		ID:    claims.UserId,
		Email: claims.Email,
	}, nil
}

func (s *JwtService) IsValid(tokenStr string) bool {
	tokenStr = trimPrefix(tokenStr)
	claims, err := parseToken(tokenStr, s.securityCfg.Secret)
	if err != nil {
		slog.Error(errMsgPraseToken, "error", err, "token", tokenStr)
		return false
	}

	if claims.ID == "" {
		slog.Error("The id is empty in token claims")
		return false
	}
	exists, err := s.redis.Exists(context.Background(), constant.RedisKeyBlackListToken+claims.ID).Result()
	if err != nil {
		slog.Error("Failed to check existence of blacklist key", "error", errors.WithStack(err))
		return false
	}
	return exists == 0
}

func (s *JwtService) Refresh(tokenStr string) (*response.AuthTokens, error) {
	tokenStr = trimPrefix(tokenStr)
	claims, err := parseToken(tokenStr, s.securityCfg.Secret)
	if err != nil {
		slog.Error(errMsgPraseToken, "error", err, "token", tokenStr)
	}

	user := &dto.User{
		ID:    claims.UserId,
		Email: claims.Email,
		Name:  claims.Subject,
	}
	accessToken, err := s.generateToken(user, s.securityCfg.AccessTokenTTL)
	if err != nil {
		return nil, err
	}
	return &response.AuthTokens{AccessToken: accessToken, RefreshToken: tokenStr}, nil
}

func (s *JwtService) Invalidate(tokenStr string) error {
	tokenStr = trimPrefix(tokenStr)
	claims, err := parseToken(tokenStr, s.securityCfg.Secret)
	if err != nil {
		return err
	}

	if time.Now().After(claims.ExpiresAt()) {
		// Token已过期，直接返回
		return nil
	}
	// 计算Token剩余时间，将其加入黑名单
	sub := time.Until(time.Unix(claims.Expiry, 0))
	return s.redis.Set(context.Background(), constant.RedisKeyBlackListToken+claims.ID, "", sub).Err()
}

func (s *JwtService) generateToken(user *dto.User, ttl int) (string, error) {
	claims := &dto.UserClaims{
		Claims: &jwt.Claims{
			IssuedAt: time.Now().Unix(),
			Subject:  user.Name,
			ID:       uuid.NewString(),
		},
		UserId: user.ID,
		Email:  user.Email,
	}
	if ttl != -1 {
		claims.Expiry = time.Now().Add(time.Duration(ttl) * time.Second).Unix()
	}

	signedJwt, err := jwt.Sign(jwt.HS256, []byte(s.securityCfg.Secret), claims)
	if err != nil {
		return "", errs.NewInternalServerErr(err, "Failed to sign token for userId %d", user.ID)
	}
	return string(signedJwt), nil
}

func trimPrefix(token string) string {
	return strings.Replace(token, "Bearer ", "", 1)
}

func parseToken(tokenStr string, secret string) (*dto.UserClaims, error) {
	verifiedToken, err := jwt.Verify(jwt.HS256, []byte(secret), []byte(tokenStr))
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to verify token: %s", tokenStr)
	}

	var claims dto.UserClaims
	err = verifiedToken.Claims(&claims)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to parse token: %s", tokenStr)
	}
	return &claims, nil
}
