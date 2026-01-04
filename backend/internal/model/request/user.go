package request

type UserLogin struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password"`
}

type UserRegister struct {
	Email      string `json:"email" binding:"required,email"`
	Name       string `json:"name" binding:"required"`
	Password   string `json:"password" binding:"required,min=6"`
	VerifyCode string `json:"verifyCode" binding:"required,min=4,max=4"`
}

type UserSearch struct {
	ID    *uint   `json:"id"`
	Name  *string `json:"name"`
	Email *string `json:"email"`
}

type UserUpdate struct {
	Name       *string `json:"name"`
	Email      *string `json:"email"`
	VerifyCode *string `json:"verifyCode" binding:"min=4,max=4"`
}

type RefreshTokenReq struct {
	RefreshToken string `json:"refresh_token"`
}

type SendVerifyCodeReq struct {
	Email string `json:"email" binding:"required,email"`
}
