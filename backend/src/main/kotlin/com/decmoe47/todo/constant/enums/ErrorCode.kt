package com.decmoe47.todo.constant.enums

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus, val code: Int, val message: String) {
    // ===== 系统 =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 10000, "Internal server error."),

    NO_QUERY_PARAM_PROVIDED(HttpStatus.BAD_REQUEST, 10001, "No query parameters provided."),
    NO_BODY_PARAM_PROVIDED(HttpStatus.BAD_REQUEST, 10002, "No parameters provided in the request body."),
    INVALID_REQUEST_PARAMS(HttpStatus.BAD_REQUEST, 10003, "Invalid request parameters."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, 10004, "Access denied."),

    // ===== 认证 =====
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 10100, "You are not logged in. Please log in and try again."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 10101, "Your session has expired. Please log in again."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 10102, "Your session has expired. Please log in again."),
    USERNAME_OR_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, 10103, "Incorrect username or password."),

    // ===== 验证码 =====
    VERIFICATION_CODE_SEND_FAILED(
        HttpStatus.SERVICE_UNAVAILABLE,
        10200,
        "Failed to send verification code. Please try again."
    ),
    VERIFICATION_CODE_EXPIRED(
        HttpStatus.UNPROCESSABLE_ENTITY,
        10201,
        "Verification code expired. Please request a new one."
    ),
    VERIFICATION_CODE_INCORRECT(HttpStatus.UNPROCESSABLE_ENTITY, 10202, "Incorrect verification code."),

    // ===== 权限 =====
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, 10300, "You do not have permission to perform this operation."),

    // ===== 用户 =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 10400, "User not found."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, 10401, "User already exists."),

    // ===== Todo =====
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, 10500, "Todo not found or has been deleted."),
    TODO_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, 10501, "Todo list not found.");
}
