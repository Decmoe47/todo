package service

import (
	"context"
	"testing"
	"todo/internal/config"
	"todo/internal/model/dto"

	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/require"
)

func newTestRedis(t *testing.T) (*miniredis.Miniredis, *redis.Client) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	client := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	return mr, client
}

func TestJwtServiceGenerateParseInvalidate(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	svc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)

	user := &dto.User{ID: 7, Email: "a@b.com", Name: "name"}
	tokens, err := svc.Generate(user)
	require.NoError(t, err)
	require.NotEmpty(t, tokens.AccessToken)
	require.NotEmpty(t, tokens.RefreshToken)

	parsed, err := svc.Parse("Bearer " + tokens.AccessToken)
	require.NoError(t, err)
	require.Equal(t, user.ID, parsed.ID)
	require.Equal(t, user.Email, parsed.Email)

	require.True(t, svc.IsValid(tokens.AccessToken))
	require.NoError(t, svc.Invalidate(tokens.AccessToken))
	require.False(t, svc.IsValid(tokens.AccessToken))
}

func TestJwtServiceRefresh(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	svc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)

	user := &dto.User{ID: 8, Email: "c@d.com", Name: "name"}
	tokens, err := svc.Generate(user)
	require.NoError(t, err)

	refreshed, err := svc.Refresh(tokens.RefreshToken)
	require.NoError(t, err)
	require.NotEmpty(t, refreshed.AccessToken)
	require.Equal(t, tokens.RefreshToken, refreshed.RefreshToken)
}

func TestJwtServiceInvalidToken(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	svc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)

	require.False(t, svc.IsValid("bad.token"))
	_, err = svc.Parse("bad.token")
	require.Error(t, err)
}

func TestJwtServiceIsValidRedisError(t *testing.T) {
	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, redis.NewClient(&redis.Options{Addr: "127.0.0.1:1"}))
	require.NoError(t, err)

	user := &dto.User{ID: 10, Email: "a@b.com", Name: "name"}
	tokens, err := jwtSvc.Generate(user)
	require.NoError(t, err)

	require.False(t, jwtSvc.IsValid(tokens.AccessToken))
}

func TestJwtServiceInvalidateExpired(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	svc, err := NewJwtService(&config.Security{AccessTokenTTL: -1, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)

	user := &dto.User{ID: 9, Email: "e@f.com", Name: "name"}
	tokens, err := svc.Generate(user)
	require.NoError(t, err)

	require.NoError(t, svc.Invalidate(tokens.AccessToken))
	_ = rdb.Del(context.Background(), "*:")
}
