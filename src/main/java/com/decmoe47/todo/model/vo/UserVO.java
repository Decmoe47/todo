package com.decmoe47.todo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private int id;
    private String email;
    private String name;
    private int level;
    private long exp;
    private boolean isLocked;
    private boolean isEnabled;
    private String loginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accountExpireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime credentialExpireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    private LocalDateTime registerTime;
}
