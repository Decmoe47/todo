package main

import (
	"context"
	"fmt"
	"log"
	"log/slog"
	"os"
	"time"
	"todo/internal/app"
	"todo/internal/config"
	"todo/internal/database"

	"github.com/gin-gonic/gin"
)

func main() {
	if err := run("config.yaml", os.Exit); err != nil {
		log.Fatal(err)
	}
}

func run(cfgPath string, exit func(int)) (err error) {
	cfg, err := config.Load(cfgPath)
	if err != nil {
		return err
	}
	router := gin.Default()
	redis, err := database.NewRedisClient(&cfg.Redis)
	if err != nil {
		return err
	}

	a, err := app.New(&app.NewOptions{
		Cfg:    cfg,
		Router: router,
		Redis:  redis,
	})
	if err != nil {
		return err
	}

	defer func() {
		if r := recover(); r != nil {
			slog.Error("panic recovered in main", "panic", r)
			if a != nil {
				_, cancel := context.WithTimeout(context.Background(), 5*time.Second)
				defer cancel()
				if shutdownErr := a.Shutdown(); shutdownErr != nil {
					err = shutdownErr
				}
			}
			if err == nil {
				err = fmt.Errorf("panic: %v", r)
			}
			exit(1)
		}
	}()

	if err := a.Run(); err != nil {
		return err
	}
	return nil
}
