package dto

import "github.com/kataras/jwt"

type UserClaims struct {
	*jwt.Claims
	UserId uint   `json:"userId"`
	Email  string `json:"email"`
}
