package service

import (
	"context"
	"fmt"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/dto"
	"todo/internal/model/entity"
	"todo/internal/model/request"
	"todo/internal/model/response"
	"todo/internal/repository"

	"github.com/cockroachdb/errors"
	"github.com/redis/go-redis/v9"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

type AuthService struct {
	jwtSvc        *JwtService
	verifyCodeSvc *VerifyCodeService
	mailSvc       *MailService
	redis         *redis.Client
	userRepo      repository.User
}

type NewAuthServiceOptions struct {
	JwtSvc        *JwtService
	VerifyCodeSvc *VerifyCodeService
	MailSvc       *MailService
	Redis         *redis.Client
	UserRepo      repository.User
}

func NewAuthService(opts *NewAuthServiceOptions) *AuthService {
	return &AuthService{
		jwtSvc:        opts.JwtSvc,
		verifyCodeSvc: opts.VerifyCodeSvc,
		mailSvc:       opts.MailSvc,
		redis:         opts.Redis,
		userRepo:      opts.UserRepo,
	}
}

func (s *AuthService) Login(ctx context.Context, userLoginReq *request.UserLogin) (*response.User, error) {
	user, err := s.auth(ctx, userLoginReq.Email, userLoginReq.Password)
	if err != nil {
		return nil, err
	}

	authTokens, err := s.jwtSvc.Generate(user)
	if err != nil {
		return nil, err
	}
	return &response.User{
		ID:         user.ID,
		Name:       user.Name,
		Email:      user.Email,
		AuthTokens: authTokens,
	}, nil
}

func (s *AuthService) auth(ctx context.Context, email, password string) (*dto.User, error) {
	user, err := s.userRepo.GetByEmail(ctx, email)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewBusinessErr(respcode.UserNotFound)
	} else if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to get user by email")
	}

	if err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password)); err != nil {
		return nil, errs.NewBusinessErr(respcode.UsernameOrPasswordIncorrect)
	}

	return &dto.User{
		ID:    user.ID,
		Email: user.Email,
		Name:  user.Name,
	}, nil
}

func (s *AuthService) Register(ctx context.Context, userRegisterReq *request.UserRegister) (*response.User, error) {
	hasUser, err := s.userRepo.HasByEmail(ctx, userRegisterReq.Email)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to count user by email")
	} else if hasUser {
		return nil, errs.NewBusinessErr(respcode.UserAlreadyExists)
	}

	if err = s.verifyCodeSvc.CheckCode(ctx, userRegisterReq.VerifyCode, userRegisterReq.Email); err != nil {
		return nil, err
	}

	password, err := encodePassword(userRegisterReq.Password)
	if err != nil {
		return nil, err
	}

	user := &entity.User{
		Email:    userRegisterReq.Email,
		Name:     userRegisterReq.Name,
		Password: password,
	}
	err = s.userRepo.Save(ctx, user)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to save user")
	}

	authTokens, err := s.jwtSvc.Generate(&dto.User{
		ID:    user.ID,
		Email: user.Email,
		Name:  user.Name,
	})
	if err != nil {
		return nil, err
	}

	return &response.User{
		ID:         user.ID,
		Name:       user.Name,
		Email:      user.Email,
		AuthTokens: authTokens,
	}, nil
}

func encodePassword(password string) (string, error) {
	encodedPassword, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return "", errs.NewInternalServerErr(err, "Failed to encode password")
	}
	return string(encodedPassword), nil
}

func (s *AuthService) Logout(token string) error {
	return s.jwtSvc.Invalidate(token)
}

func (s *AuthService) RefreshAccessToken(req *request.RefreshTokenReq) (*response.AuthTokens, error) {
	if !s.jwtSvc.IsValid(req.RefreshToken) {
		return nil, errs.NewBusinessErr(respcode.RefreshTokenExpired)
	}
	return s.jwtSvc.Refresh(req.RefreshToken)
}

func (s *AuthService) SendVerifyCode(ctx context.Context, req *request.SendVerifyCodeReq) error {
	verifyCode, err := s.verifyCodeSvc.CreateCode(ctx, req.Email)
	if err != nil {
		return err
	}

	err = s.mailSvc.Send([]string{req.Email}, fmt.Sprintf(constant.VerifyCodeMailBody, verifyCode))
	if err != nil {
		return err
	}
	return nil
}
