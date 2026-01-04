package handler

import (
	"bufio"
	"net"
	"strings"
	"testing"
	"todo/internal/config"
	"todo/internal/model/entity"
	"todo/internal/repository"
	"todo/internal/repository/impl"
	"todo/internal/service"

	"github.com/alicebob/miniredis/v2"
	"github.com/glebarez/sqlite"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

type testDeps struct {
	db        *gorm.DB
	redis     *redis.Client
	miniredis *miniredis.Miniredis
}

func newTestDeps(t *testing.T) *testDeps {
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	require.NoError(t, err)
	require.NoError(t, db.AutoMigrate(&entity.User{}, &entity.TodoList{}, &entity.Todo{}))

	mr, err := miniredis.Run()
	require.NoError(t, err)
	client := redis.NewClient(&redis.Options{Addr: mr.Addr()})

	return &testDeps{db: db, redis: client, miniredis: mr}
}

func (d *testDeps) close() {
	if d.redis != nil {
		_ = d.redis.Close()
	}
	if d.miniredis != nil {
		d.miniredis.Close()
	}
}

func newJwtSvc(t *testing.T, redis *redis.Client) *service.JwtService {
	jwtSvc, err := service.NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, redis)
	require.NoError(t, err)
	return jwtSvc
}

func newAuthSvc(t *testing.T, deps *testDeps, mailSvc *service.MailService) *service.AuthService {
	verifySvc, err := service.NewVerifyCodeService(deps.redis)
	require.NoError(t, err)
	return service.NewAuthService(&service.NewAuthServiceOptions{
		JwtSvc:        newJwtSvc(t, deps.redis),
		VerifyCodeSvc: verifySvc,
		MailSvc:       mailSvc,
		UserRepo:      impl.NewUserRepository(deps.db),
	})
}

func newUserSvc(t *testing.T, deps *testDeps) *service.UserService {
	verifySvc, err := service.NewVerifyCodeService(deps.redis)
	require.NoError(t, err)
	return service.NewUserService(&service.NewUserServiceOptions{
		JwtSvc:        newJwtSvc(t, deps.redis),
		VerifyCodeSvc: verifySvc,
		UserRepo:      impl.NewUserRepository(deps.db),
	})
}

func newTodoSvc(t *testing.T, deps *testDeps) *service.TodoService {
	return service.NewTodoService(&service.NewTodoServiceOptions{
		TodoRepo:     impl.NewTodoRepository(deps.db),
		TodoListRepo: impl.NewTodoListRepository(deps.db),
		UserRepo:     impl.NewUserRepository(deps.db),
	})
}

func newTodoListSvc(t *testing.T, deps *testDeps) *service.TodoListService {
	return service.NewTodoListService(&service.NewTodoListServiceOptions{
		TodoListRepo: impl.NewTodoListRepository(deps.db),
		TodoRepo:     impl.NewTodoRepository(deps.db),
		UserRepo:     impl.NewUserRepository(deps.db),
		TxMgr:        repository.NewGormTxManager(deps.db, nil),
	})
}

func startSMTPServer(t *testing.T) (string, func()) {
	ln, err := net.Listen("tcp", "127.0.0.1:0")
	require.NoError(t, err)

	stop := make(chan struct{})
	go func() {
		defer ln.Close()
		for {
			conn, err := ln.Accept()
			if err != nil {
				return
			}
			go handleSMTPConn(t, conn)
			select {
			case <-stop:
				return
			default:
			}
		}
	}()

	return ln.Addr().String(), func() { close(stop); _ = ln.Close() }
}

func handleSMTPConn(t *testing.T, conn net.Conn) {
	defer conn.Close()
	writeLine(t, conn, "220 localhost")

	reader := bufio.NewReader(conn)
	for {
		line, err := reader.ReadString('\n')
		if err != nil {
			return
		}
		cmd := strings.TrimSpace(line)
		if cmd == "" {
			continue
		}

		switch {
		case strings.HasPrefix(cmd, "EHLO") || strings.HasPrefix(cmd, "HELO"):
			writeLine(t, conn, "250-localhost")
			writeLine(t, conn, "250 AUTH PLAIN")
		case strings.HasPrefix(cmd, "AUTH"):
			writeLine(t, conn, "235 2.7.0 OK")
		case strings.HasPrefix(cmd, "MAIL FROM"):
			writeLine(t, conn, "250 OK")
		case strings.HasPrefix(cmd, "RCPT TO"):
			writeLine(t, conn, "250 OK")
		case strings.HasPrefix(cmd, "DATA"):
			writeLine(t, conn, "354 End data with <CR><LF>.<CR><LF>")
			for {
				dataLine, err := reader.ReadString('\n')
				if err != nil {
					return
				}
				if strings.TrimSpace(dataLine) == "." {
					writeLine(t, conn, "250 OK")
					break
				}
			}
		case strings.HasPrefix(cmd, "QUIT"):
			writeLine(t, conn, "221 Bye")
			return
		default:
			writeLine(t, conn, "250 OK")
		}
	}
}

func writeLine(t *testing.T, conn net.Conn, line string) {
	_, err := conn.Write([]byte(line + "\r\n"))
	require.NoError(t, err)
}

func mustAtoi(t *testing.T, s string) int {
	var n int
	for _, ch := range s {
		n = n*10 + int(ch-'0')
	}
	return n
}
