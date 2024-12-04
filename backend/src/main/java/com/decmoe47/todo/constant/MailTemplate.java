package com.decmoe47.todo.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailTemplate {

    public static final String VERIFY_CODE_SUBJECT = "验证码";

    public static final String VERIFY_CODE_BODY = """
            您的验证码是：
            
            {code}
            
            验证码5分钟内有效。
            """;

    public static final String AT_MESSAGE_SUBJECT = "有人@了你";
    public static final String DIRECT_MESSAGE_SUBJECT = "你有一条新的私信";
}
