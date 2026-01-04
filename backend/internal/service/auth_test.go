package service

import (
	"bufio"
	"context"
	"net"
	"strings"
	"testing"
	"time"
	"todo/internal/config"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/dto"
	"todo/internal/model/entity"
	"todo/internal/model/request"

	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

func TestAuthLoginSuccess(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)

	userRepo := &MockUserRepo{}
	password, err := bcrypt.GenerateFromPassword([]byte("pass"), bcrypt.DefaultCost)
	require.NoError(t, err)
	userRepo.On("GetByEmail", mockAnyContext, "a@b.com").Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}, Email: "a@b.com", Name: "A", Password: string(password)}, nil)

	svc := NewAuthService(&NewAuthServiceOptions{JwtSvc: jwtSvc, UserRepo: userRepo})
	resp, err := svc.Login(context.Background(), &request.UserLogin{Email: "a@b.com", Password: "pass"})
	require.NoError(t, err)
	require.Equal(t, uint(1), resp.ID)
	require.NotEmpty(t, resp.AuthTokens.AccessToken)
	require.NotEmpty(t, resp.AuthTokens.RefreshToken)
}

func TestAuthLoginNotFound(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByEmail", mockAnyContext, "a@b.com").Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	svc := NewAuthService(&NewAuthServiceOptions{UserRepo: userRepo})
	_, err := svc.Login(context.Background(), &request.UserLogin{Email: "a@b.com", Password: "pass"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.UserNotFound, be.Code)
}

func TestAuthLoginWrongPassword(t *testing.T) {
	userRepo := &MockUserRepo{}
	password, err := bcrypt.GenerateFromPassword([]byte("pass"), bcrypt.DefaultCost)
	require.NoError(t, err)
	userRepo.On("GetByEmail", mockAnyContext, "a@b.com").Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}, Email: "a@b.com", Name: "A", Password: string(password)}, nil)

	svc := NewAuthService(&NewAuthServiceOptions{UserRepo: userRepo})
	_, err = svc.Login(context.Background(), &request.UserLogin{Email: "a@b.com", Password: "bad"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.UsernameOrPasswordIncorrect, be.Code)
}

func TestAuthRegisterUserExists(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("HasByEmail", mockAnyContext, "a@b.com").Return(true, nil)

	svc := NewAuthService(&NewAuthServiceOptions{UserRepo: userRepo})
	_, err := svc.Register(context.Background(), &request.UserRegister{Email: "a@b.com", Password: "pass", Name: "A", VerifyCode: "1234"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.UserAlreadyExists, be.Code)
}

func TestAuthRegisterHasByEmailError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("HasByEmail", mockAnyContext, "a@b.com").Return(false, errs.ErrVerifyToken)

	svc := NewAuthService(&NewAuthServiceOptions{UserRepo: userRepo})
	_, err := svc.Register(context.Background(), &request.UserRegister{Email: "a@b.com", Password: "pass", Name: "A", VerifyCode: "1234"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestAuthRegisterSuccess(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)
	verifySvc, err := NewVerifyCodeService(rdb)
	require.NoError(t, err)

	userRepo := &MockUserRepo{}
	userRepo.On("HasByEmail", mockAnyContext, "a@b.com").Return(false, nil)
	userRepo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.User")).Run(func(args mock.Arguments) {
		u := args.Get(1).(*entity.User)
		u.Auditable = &entity.Auditable{Model: &gorm.Model{ID: 9}}
	}).Return(nil)

	require.NoError(t, rdb.Set(context.Background(), constant.RedisKeyVerifyCode+"a@b.com", "1234", time.Minute).Err())

	svc := NewAuthService(&NewAuthServiceOptions{JwtSvc: jwtSvc, VerifyCodeSvc: verifySvc, UserRepo: userRepo})
	resp, err := svc.Register(context.Background(), &request.UserRegister{Email: "a@b.com", Password: "pass", Name: "A", VerifyCode: "1234"})
	require.NoError(t, err)
	require.Equal(t, "a@b.com", resp.Email)
	require.NotEmpty(t, resp.AuthTokens.AccessToken)
}

func TestAuthRegisterSaveError(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	verifySvc, err := NewVerifyCodeService(rdb)
	require.NoError(t, err)

	userRepo := &MockUserRepo{}
	userRepo.On("HasByEmail", mockAnyContext, "a@b.com").Return(false, nil)
	userRepo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.User")).Return(errs.ErrVerifyToken)

	require.NoError(t, rdb.Set(context.Background(), constant.RedisKeyVerifyCode+"a@b.com", "1234", time.Minute).Err())

	svc := NewAuthService(&NewAuthServiceOptions{VerifyCodeSvc: verifySvc, UserRepo: userRepo})
	_, err = svc.Register(context.Background(), &request.UserRegister{Email: "a@b.com", Password: "pass", Name: "A", VerifyCode: "1234"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestAuthRegisterVerifyCodeError(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	verifySvc, err := NewVerifyCodeService(rdb)
	require.NoError(t, err)

	userRepo := &MockUserRepo{}
	userRepo.On("HasByEmail", mockAnyContext, "a@b.com").Return(false, nil)

	svc := NewAuthService(&NewAuthServiceOptions{VerifyCodeSvc: verifySvc, UserRepo: userRepo})
	_, err = svc.Register(context.Background(), &request.UserRegister{Email: "a@b.com", Password: "pass", Name: "A", VerifyCode: "9999"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.VerificationCodeExpired, be.Code)
}

func TestAuthLoginInternalError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByEmail", mockAnyContext, "a@b.com").Return((*entity.User)(nil), errs.ErrVerifyToken)

	svc := NewAuthService(&NewAuthServiceOptions{UserRepo: userRepo})
	_, err := svc.Login(context.Background(), &request.UserLogin{Email: "a@b.com", Password: "pass"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestAuthLogout(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)
	tokens, err := jwtSvc.Generate(&dto.User{ID: 1, Email: "a@b.com", Name: "A"})
	require.NoError(t, err)

	svc := NewAuthService(&NewAuthServiceOptions{JwtSvc: jwtSvc})
	require.NoError(t, svc.Logout(tokens.AccessToken))
	require.False(t, jwtSvc.IsValid(tokens.AccessToken))
}

func TestAuthLogoutInvalidToken(t *testing.T) {
	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, redis.NewClient(&redis.Options{Addr: "127.0.0.1:1"}))
	require.NoError(t, err)

	svc := NewAuthService(&NewAuthServiceOptions{JwtSvc: jwtSvc})
	err = svc.Logout("bad.token")
	require.Error(t, err)
}

func TestAuthRefreshToken(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)
	tokens, err := jwtSvc.Generate(&dto.User{ID: 1, Email: "a@b.com", Name: "A"})
	require.NoError(t, err)

	svc := NewAuthService(&NewAuthServiceOptions{JwtSvc: jwtSvc})
	resp, err := svc.RefreshAccessToken(&request.RefreshTokenReq{RefreshToken: tokens.RefreshToken})
	require.NoError(t, err)
	require.NotEmpty(t, resp.AccessToken)
}

func TestAuthRefreshTokenInvalid(t *testing.T) {
	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, redis.NewClient(&redis.Options{Addr: "127.0.0.1:1"}))
	require.NoError(t, err)

	svc := NewAuthService(&NewAuthServiceOptions{JwtSvc: jwtSvc})
	_, err = svc.RefreshAccessToken(&request.RefreshTokenReq{RefreshToken: "bad.token"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.RefreshTokenExpired, be.Code)
}

func TestAuthSendVerifyCode(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	verifySvc, err := NewVerifyCodeService(rdb)
	require.NoError(t, err)

	addr, stop := startSMTPServer(t)
	defer stop()

	parts := strings.Split(addr, ":")
	mailSvc := NewMail(&config.Mail{Host: parts[0], Port: mustAtoi(t, parts[1]), Username: "u", Password: "p", From: "from@test"})

	svc := NewAuthService(&NewAuthServiceOptions{VerifyCodeSvc: verifySvc, MailSvc: mailSvc})
	require.NoError(t, svc.SendVerifyCode(context.Background(), &request.SendVerifyCodeReq{Email: "a@b.com"}))

	stored, err := mr.Get(constant.RedisKeyVerifyCode + "a@b.com")
	require.NoError(t, err)
	require.Len(t, stored, 4)
}

func TestAuthSendVerifyCodeMailError(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	verifySvc, err := NewVerifyCodeService(rdb)
	require.NoError(t, err)

	mailSvc := NewMail(&config.Mail{Host: "127.0.0.1", Port: 1, Username: "u", Password: "p", From: "from@test"})
	svc := NewAuthService(&NewAuthServiceOptions{VerifyCodeSvc: verifySvc, MailSvc: mailSvc})
	err = svc.SendVerifyCode(context.Background(), &request.SendVerifyCodeReq{Email: "a@b.com"})
	require.Error(t, err)
}

var mockAnyContext = mock.Anything

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
