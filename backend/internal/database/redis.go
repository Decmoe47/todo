package database

import (
	"context"
	"strconv"
	"todo/internal/config"

	"github.com/redis/go-redis/v9"
)

func NewRedisClient(cfg *config.Redis) (*redis.Client, error) {
	rdb := redis.NewClient(&redis.Options{ // Be closed in App.Shutdown() which is deferred in main() to prevent panic situation
		Addr:     cfg.Host + ":" + strconv.Itoa(cfg.Port),
		Password: cfg.Password,
		DB:       0,
	})

	if _, err := rdb.Ping(context.Background()).Result(); err != nil {
		return nil, err
	}
	return rdb, nil
}
