package service

import (
	"context"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/entity"
	"todo/internal/model/request"
	"todo/internal/model/response"
	"todo/internal/repository"
	"todo/internal/util"

	"github.com/cockroachdb/errors"
	"gorm.io/gorm"
)

type UserService struct {
	jwtSvc        *JwtService
	verifyCodeSvc *VerifyCodeService
	userRepo      repository.User
}

type NewUserServiceOptions struct {
	JwtSvc        *JwtService
	VerifyCodeSvc *VerifyCodeService
	UserRepo      repository.User
}

func NewUserService(opts *NewUserServiceOptions) *UserService {
	return &UserService{
		jwtSvc:        opts.JwtSvc,
		verifyCodeSvc: opts.VerifyCodeSvc,
		userRepo:      opts.UserRepo,
	}
}

func toUserResponse(user *entity.User) *response.User {
	return &response.User{
		ID:    user.ID,
		Name:  user.Name,
		Email: user.Email,
	}
}

func (s *UserService) GetUser(ctx context.Context, id uint) (*response.User, error) {
	user, err := s.userRepo.GetByID(ctx, id)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewBusinessErr(respcode.UserNotFound)
	} else if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to get user by id")
	}

	return toUserResponse(user), nil
}

func (s *UserService) SearchUser(ctx context.Context, searchReq *request.UserSearch) ([]*response.User, error) {
	if searchReq.ID != nil {
		u, err := s.userRepo.GetByID(ctx, *searchReq.ID)
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errs.NewBusinessErr(respcode.UserNotFound)
		} else if err != nil {
			return nil, errs.NewInternalServerErr(err, "Failed to get user by id")
		}
		return []*response.User{toUserResponse(u)}, nil
	}

	if searchReq.Email != nil {
		users, err := s.userRepo.SelectByEmail(ctx, *searchReq.Email)
		if err != nil {
			return nil, errs.NewInternalServerErr(err, "Failed to search users by email")
		}
		if len(users) == 0 {
			return nil, errs.NewBusinessErr(respcode.UserNotFound)
		}
		return util.MapSlice(users, toUserResponse), nil
	}

	if searchReq.Name != nil {
		users, err := s.userRepo.SelectByName(ctx, *searchReq.Name)
		if err != nil {
			return nil, errs.NewInternalServerErr(err, "Failed to search users by name")
		}
		return util.MapSlice(users, toUserResponse), nil
	}

	return nil, errs.NewBusinessErr(respcode.NoQueryParamProvided)
}

func (s *UserService) UpdateUser(ctx context.Context, userID uint, updateReq *request.UserUpdate) (*response.User, error) {
	user, err := s.userRepo.GetByID(ctx, userID)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewBusinessErr(respcode.UserNotFound)
	} else if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to get user by id")
	}

	if updateReq.Name != nil {
		user.Name = *updateReq.Name
	}

	if updateReq.Email != nil {
		if updateReq.VerifyCode == nil {
			return nil, errs.NewBusinessErr(respcode.InvalidParam)
		}
		if err := s.verifyCodeSvc.CheckCode(ctx, *updateReq.VerifyCode, *updateReq.Email); err != nil {
			return nil, err
		}
		user.Email = *updateReq.Email
	}

	if err := s.userRepo.Save(ctx, user); err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to save user")
	}

	return toUserResponse(user), nil
}

func (s *UserService) GetUserByToken(ctx context.Context, token string) (*response.User, error) {
	if !s.jwtSvc.IsValid(token) {
		return nil, errs.NewBusinessErr(respcode.AccessTokenExpired)
	}

	userDTO, err := s.jwtSvc.Parse(token)
	if err != nil {
		return nil, err
	}

	user, err := s.userRepo.GetByID(ctx, userDTO.ID)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewBusinessErr(respcode.UserNotFound)
	} else if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to get user by id")
	}
	return toUserResponse(user), nil
}
