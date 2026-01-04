package service

import (
	"context"
	"testing"
	"time"
	"todo/internal/config"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/dto"
	"todo/internal/model/entity"
	"todo/internal/model/request"

	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func TestUserGetUser(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}, Email: "a@b.com", Name: "A"}, nil)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	resp, err := svc.GetUser(context.Background(), 1)
	require.NoError(t, err)
	require.Equal(t, uint(1), resp.ID)
}

func TestUserGetUserNotFound(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	_, err := svc.GetUser(context.Background(), 1)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.UserNotFound, be.Code)
}

func TestUserGetUserError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), errs.ErrVerifyToken)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	_, err := svc.GetUser(context.Background(), 1)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestUserSearchByID(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 2}}, Email: "a@b.com", Name: "A"}, nil)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	id := uint(2)
	resp, err := svc.SearchUser(context.Background(), &request.UserSearch{ID: &id})
	require.NoError(t, err)
	require.Len(t, resp, 1)
}

func TestUserSearchByEmailNotFound(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("SelectByEmail", mockAnyContext, "a@b.com").Return([]*entity.User{}, nil)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	email := "a@b.com"
	_, err := svc.SearchUser(context.Background(), &request.UserSearch{Email: &email})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.UserNotFound, be.Code)
}

func TestUserSearchByEmailError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("SelectByEmail", mockAnyContext, "a@b.com").Return([]*entity.User{}, errs.ErrVerifyToken)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	email := "a@b.com"
	_, err := svc.SearchUser(context.Background(), &request.UserSearch{Email: &email})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestUserSearchByName(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("SelectByName", mockAnyContext, "A").Return([]*entity.User{{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}, Email: "a@b.com", Name: "A"}}, nil)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	name := "A"
	resp, err := svc.SearchUser(context.Background(), &request.UserSearch{Name: &name})
	require.NoError(t, err)
	require.Len(t, resp, 1)
}

func TestUserSearchByNameError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("SelectByName", mockAnyContext, "A").Return([]*entity.User{}, errs.ErrVerifyToken)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	name := "A"
	_, err := svc.SearchUser(context.Background(), &request.UserSearch{Name: &name})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestUserSearchNoParams(t *testing.T) {
	svc := NewUserService(&NewUserServiceOptions{UserRepo: &MockUserRepo{}})
	_, err := svc.SearchUser(context.Background(), &request.UserSearch{})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.NoQueryParamProvided, be.Code)
}

func TestUserUpdateNameOnly(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}}, nil)
	userRepo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.User")).Return(nil)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	name := "B"
	resp, err := svc.UpdateUser(context.Background(), 1, &request.UserUpdate{Name: &name})
	require.NoError(t, err)
	require.Equal(t, "B", resp.Name)
}

func TestUserUpdateEmailRequiresVerifyCode(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}}, nil)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	email := "a@b.com"
	_, err := svc.UpdateUser(context.Background(), 1, &request.UserUpdate{Email: &email})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InvalidParam, be.Code)
}

func TestUserUpdateEmailWithVerifyCode(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	rdb := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	verifySvc, err := NewVerifyCodeService(rdb)
	require.NoError(t, err)
	require.NoError(t, rdb.Set(context.Background(), constant.RedisKeyVerifyCode+"a@b.com", "1234", time.Minute).Err())

	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}}, nil)
	userRepo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.User")).Return(nil)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo, VerifyCodeSvc: verifySvc})
	email := "a@b.com"
	code := "1234"
	resp, err := svc.UpdateUser(context.Background(), 1, &request.UserUpdate{Email: &email, VerifyCode: &code})
	require.NoError(t, err)
	require.Equal(t, email, resp.Email)
}

func TestUserUpdateSaveError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}}, nil)
	userRepo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.User")).Return(errs.ErrVerifyToken)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo})
	name := "B"
	_, err := svc.UpdateUser(context.Background(), 1, &request.UserUpdate{Name: &name})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestUserUpdateEmailVerifyCodeError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}}, nil)

	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	rdb := redis.NewClient(&redis.Options{Addr: mr.Addr()})
	verifySvc, err := NewVerifyCodeService(rdb)
	require.NoError(t, err)

	svc := NewUserService(&NewUserServiceOptions{UserRepo: userRepo, VerifyCodeSvc: verifySvc})
	email := "a@b.com"
	code := "1234"
	_, err = svc.UpdateUser(context.Background(), 1, &request.UserUpdate{Email: &email, VerifyCode: &code})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.VerificationCodeExpired, be.Code)
}

func TestUserGetUserByTokenInvalid(t *testing.T) {
	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, redis.NewClient(&redis.Options{Addr: "127.0.0.1:1"}))
	require.NoError(t, err)

	svc := NewUserService(&NewUserServiceOptions{JwtSvc: jwtSvc, UserRepo: &MockUserRepo{}})
	_, err = svc.GetUserByToken(context.Background(), "bad.token")
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.AccessTokenExpired, be.Code)
}

func TestUserGetUserByTokenNotFound(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)
	tokens, err := jwtSvc.Generate(&dto.User{ID: 1, Email: "a@b.com", Name: "A"})
	require.NoError(t, err)

	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	svc := NewUserService(&NewUserServiceOptions{JwtSvc: jwtSvc, UserRepo: userRepo})
	_, err = svc.GetUserByToken(context.Background(), tokens.AccessToken)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.UserNotFound, be.Code)
}

func TestUserGetUserByTokenError(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)
	tokens, err := jwtSvc.Generate(&dto.User{ID: 1, Email: "a@b.com", Name: "A"})
	require.NoError(t, err)

	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), errs.ErrVerifyToken)

	svc := NewUserService(&NewUserServiceOptions{JwtSvc: jwtSvc, UserRepo: userRepo})
	_, err = svc.GetUserByToken(context.Background(), tokens.AccessToken)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestUserGetUserByToken(t *testing.T) {
	mr, rdb := newTestRedis(t)
	defer mr.Close()

	jwtSvc, err := NewJwtService(&config.Security{AccessTokenTTL: 60, RefreshTokenTTL: 120, Secret: "secret"}, rdb)
	require.NoError(t, err)
	tokens, err := jwtSvc.Generate(&dto.User{ID: 1, Email: "a@b.com", Name: "A"})
	require.NoError(t, err)

	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}, Email: "a@b.com", Name: "A"}, nil)

	svc := NewUserService(&NewUserServiceOptions{JwtSvc: jwtSvc, UserRepo: userRepo})
	resp, err := svc.GetUserByToken(context.Background(), tokens.AccessToken)
	require.NoError(t, err)
	require.Equal(t, uint(1), resp.ID)
}
