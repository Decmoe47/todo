package respcode

type RespCode struct {
	code    int
	message string
}

func (e *RespCode) Code() int {
	return e.code
}

func (e *RespCode) Message() string {
	return e.message
}

var (
	Ok                   = &RespCode{code: 0, message: ""}
	InternalServerError  = &RespCode{code: 10000, message: "服务器内部错误！"}
	NoQueryParamProvided = &RespCode{code: 10001, message: "未提供任何查询参数！"}
	NoBodyParamProvided  = &RespCode{code: 10002, message: "请求正文里未提供任何参数！"}
	InvalidParam         = &RespCode{code: 10003, message: "错误的请求参数！"}

	Unauthorized                = &RespCode{code: 10100, message: "您尚未登录，请登录后重试！"}
	UserNotFound                = &RespCode{code: 10101, message: "未找到该用户！"}
	UserAlreadyExists           = &RespCode{code: 10102, message: "已存在该账号！"}
	VerificationCodeSendFailed  = &RespCode{code: 10103, message: "验证码发送失败，请重试！"}
	VerificationCodeExpired     = &RespCode{code: 10104, message: "验证码已失效，请重新发送！"}
	VerificationCodeIncorrect   = &RespCode{code: 10105, message: "验证码错误！"}
	PermissionDenied            = &RespCode{code: 10106, message: "您没有该操作的权限！"}
	AccessTokenExpired          = &RespCode{code: 10107, message: "登录已过期，请重新登录！"}
	RefreshTokenExpired         = &RespCode{code: 10108, message: "登录已过期，请重新登录！"}
	UsernameOrPasswordIncorrect = &RespCode{code: 10109, message: "用户名或密码错误！"}
	AccessDenied                = &RespCode{code: 10110, message: "访问被拒绝！"}

	ListAlreadyExists = &RespCode{code: 10210, message: "已存在该清单！"}

	TodoNotFound     = &RespCode{code: 10200, message: "任务不存在或已被删除！"}
	TodoListNotFound = &RespCode{code: 10201, message: "不存在该清单"}
)
