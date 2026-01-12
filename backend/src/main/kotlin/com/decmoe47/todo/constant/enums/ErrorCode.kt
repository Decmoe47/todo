package com.decmoe47.todo.constant.enums

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus, val code: Int, val message: String) {
    // ===== 系统 =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 10000, "服务器内部错误！"),

    NO_QUERY_PARAM_PROVIDED(HttpStatus.BAD_REQUEST, 10001, "未提供任何查询参数！"),
    NO_BODY_PARAM_PROVIDED(HttpStatus.BAD_REQUEST, 10002, "请求正文里未提供任何参数！"),
    INVALID_REQUEST_PARAMS(HttpStatus.BAD_REQUEST, 10003, "请求参数无效！"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, 10004, "访问被拒绝！"),

    // ===== 认证 =====
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 10100, "您尚未登录，请登录后重试！"),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 10101, "登录已过期，请重新登录！"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 10102, "登录已过期，请重新登录！"),
    USERNAME_OR_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, 10103, "用户名或密码错误！"),

    // ===== 验证码 =====
    VERIFICATION_CODE_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, 10200, "验证码发送失败，请重试！"),
    VERIFICATION_CODE_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY, 10201, "验证码已失效，请重新发送！"),
    VERIFICATION_CODE_INCORRECT(HttpStatus.UNPROCESSABLE_ENTITY, 10202, "验证码错误！"),

    // ===== 权限 =====
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, 10300, "您没有该操作的权限！"),

    // ===== 用户 =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 10400, "未找到该用户！"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, 10401, "已存在该账号！"),

    // ===== Todo =====
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, 10500, "任务不存在或已被删除！"),
    TODO_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, 10501, "不存在该清单");
}