package constant

const (
	CtxKeyUser = "auth_user"
)

var (
	AuthWhiteList = []string{
		"/api/auth/login",
		"/api/auth/register",
		"/api/auth/logout",
		"/api/auth/send-verify-code",
		"/api/auth/refresh-token",
	}
)
