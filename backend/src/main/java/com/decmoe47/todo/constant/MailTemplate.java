package com.decmoe47.todo.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MailTemplate {

    public static final String VERIFICATION_CODE_SUBJECT = "[Todo APP] Your verification code";

    public static final String VERIFICATION_CODE_BODY = """
            Your verification code is:
            
            {code}
            
            The verification code is valid for 5 minutes.
            """;

    public static final String AT_MESSAGE_SUBJECT = "有人@了你";
    public static final String DIRECT_MESSAGE_SUBJECT = "你有一条新的私信";
}
