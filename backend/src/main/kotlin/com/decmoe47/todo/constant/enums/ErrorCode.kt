package com.decmoe47.todo.constant.enums

enum class ErrorCode(val code: Int, val message: String) {
    INTERNAL_SERVER_ERROR(10000, "服务器内部错误！"),
    NO_QUERY_PARAM_PROVIDED(10001, "未提供任何查询参数！"),
    NO_BODY_PARAM_PROVIDED(10002, "请求正文里未提供任何参数！"),
    INVALID_REQUEST_PARAMS(10003, "请求参数无效！"),
    ACCESS_DENIED(10004, "访问被拒绝！"),

    UNAUTHORIZED(10100, "您尚未登录，请登录后重试！"),
    USER_NOT_FOUND(10101, "未找到该用户！"),
    USER_ALREADY_EXISTS(10102, "已存在该账号！"),
    VERIFICATION_CODE_SEND_FAILED(10103, "验证码发送失败，请重试！"),
    VERIFICATION_CODE_EXPIRED(10104, "验证码已失效，请重新发送！"),
    VERIFICATION_CODE_INCORRECT(10105, "验证码错误！"),
    PERMISSION_DENIED(10106, "您没有该操作的权限！"),
    ACCESS_TOKEN_EXPIRED(10107, "登录已过期，请重新登录！"),
    REFRESH_TOKEN_EXPIRED(10108, "登录已过期，请重新登录！"),
    USERNAME_OR_PASSWORD_INCORRECT(10109, "用户名或密码错误！"),

    TODO_NOT_FOUND(10200, "任务不存在或已被删除！"),
    TODO_LIST_NOT_FOUND(10201, "不存在该清单");
}