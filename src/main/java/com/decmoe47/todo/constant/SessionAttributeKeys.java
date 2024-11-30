package com.decmoe47.todo.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionAttributeKeys {

    /**
     * 验证码缓存键名
     */
    public static final String VERIFY_CODE_FOR_USER_ID = "verifyCodeForUserId";

    /**
     * 用户ID缓存键名
     */
    public static final String USER_ID = "userId";

    public static final String EMAIL_OF_USER_ID = "emailOfUserId";
    public static final String TEAM_ID = "teamId";
    public static final String MEMBERS_OF_TEAMS = "membersOfTeamId";
    public static final String VERIFICATION_CODE = "verificationCode";
    public static final String VERIFICATION_CODE_EXPIRE_TIME = "verificationCodeExpireTime";
}
