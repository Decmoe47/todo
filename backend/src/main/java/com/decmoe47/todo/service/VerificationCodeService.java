package com.decmoe47.todo.service;

import org.jetbrains.annotations.Nullable;

public interface VerificationCodeService {

    String createCode(String email);

    String getCode(String email);

    void assertSameCode(String source, @Nullable String target);

    void checkCode(@Nullable String target, String email);
}
