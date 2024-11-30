package com.decmoe47.todo.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 返回给前端的业务异常code
 */
@Getter
@RequiredArgsConstructor
@ToString
public enum ErrorCodeEnum {

    INTERNAL_SERVER_ERROR("服务器内部错误！"),
    NO_QUERY_PARAM_PROVIDED("未提供任何查询参数！"),
    NO_BODY_PARAM_PROVIDED("请求正文里未提供任何参数！"),

    UNAUTHORIZED("您尚未登录，请登录后重试！"),
    USER_NOT_FOUND("未找到该用户！"),
    USER_ALREADY_EXISTS("已存在该账号！"),
    VERIFY_CODE_SEND_FAILED("验证码发送失败，请重试！"),
    VERIFY_CODE_EXPIRED("验证码已失效，请重新发送！"),
    VERIFY_CODE_INCORRECT("验证码错误！"),
    PERMISSION_DENIED("您没有该操作的权限！"),

    TEAM_NOT_FOUND("未找到该字幕组！"),
    ROLES_NOT_FOUND("未设立任何职位");

    private static final int START_VALUE = 10000;

    static {
        int currentValue = START_VALUE;
        for (ErrorCodeEnum instance : ErrorCodeEnum.values()) {
            instance.code = currentValue++;
        }
    }

    private final String message;
    private int code;
}
