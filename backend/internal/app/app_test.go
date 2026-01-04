package app

import (
	"testing"
	"todo/internal/config"

	"github.com/alicebob/miniredis/v2"
	"github.com/gin-gonic/gin"
	"github.com/glebarez/sqlite"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func TestAppNewAndShutdown(t *testing.T) {
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	require.NoError(t, err)

	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	rdb := redis.NewClient(&redis.Options{Addr: mr.Addr()})

	cfg := &config.Root{Security: config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}}

	app, err := New(&NewOptions{Cfg: cfg, Router: gin.New(), DB: db, Redis: rdb})
	require.NoError(t, err)
	require.NoError(t, app.Shutdown())
}
