package app

import (
	"todo/internal/config"
	"todo/internal/middleware"
	"todo/internal/repository"
	"todo/internal/repository/impl"
	"todo/internal/service"

	"github.com/cockroachdb/errors"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
	"gorm.io/gorm"
)

type App struct {
	cfg    *config.Root
	router *gin.Engine
	db     *gorm.DB
	redis  *redis.Client
}

type NewOptions struct {
	Cfg    *config.Root
	Router *gin.Engine
	DB     *gorm.DB
	Redis  *redis.Client
}

func New(opts *NewOptions) (*App, error) {
	// repos
	userRepo := impl.NewUserRepository(opts.DB)
	todoRepo := impl.NewTodoRepository(opts.DB)
	todoListRepo := impl.NewTodoListRepository(opts.DB)
	txMgr := repository.NewGormTxManager(opts.DB, nil)

	// services
	jwtSvc, err := service.NewJwtService(&opts.Cfg.Security, opts.Redis)
	if err != nil {
		return nil, err
	}
	verifyCodeSvc, err := service.NewVerifyCodeService(opts.Redis)
	if err != nil {
		return nil, err
	}
	authSvc := service.NewAuthService(&service.NewAuthServiceOptions{
		JwtSvc:        jwtSvc,
		VerifyCodeSvc: verifyCodeSvc,
		Redis:         opts.Redis,
	})
	userSvc := service.NewUserService(&service.NewUserServiceOptions{
		JwtSvc:        jwtSvc,
		VerifyCodeSvc: verifyCodeSvc,
		UserRepo:      userRepo,
	})
	todoSvc := service.NewTodoService(&service.NewTodoServiceOptions{
		TodoRepo:     todoRepo,
		TodoListRepo: todoListRepo,
		UserRepo:     userRepo,
	})
	todoListSvc := service.NewTodoListService(&service.NewTodoListServiceOptions{
		TodoListRepo: todoListRepo,
		TodoRepo:     todoRepo,
		UserRepo:     userRepo,
		TxMgr:        txMgr,
	})

	r := gin.Default()
	api := r.Group("/api")
	// middleware
	r.Use(middleware.GlobalErrorHandler())
	api.Use(middleware.JwtAuth(jwtSvc))

	// routes
	registerAuth(api, authSvc)
	registerUser(api, userSvc)
	registerTodo(api, todoSvc)
	registerTodoList(api, todoListSvc)

	return &App{cfg: opts.Cfg, db: opts.DB, redis: opts.Redis}, nil
}

func (a *App) Run() error {
	return nil
}

func (a *App) Shutdown() error {
	err := a.redis.Close()
	if err != nil {
		return errors.Wrap(err, "error closing redis connection")
	}

	return nil
}
