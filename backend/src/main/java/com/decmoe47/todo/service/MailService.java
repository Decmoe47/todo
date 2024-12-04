package com.decmoe47.todo.service;

import java.util.List;

public interface MailService {

    boolean send(List<String> to, String subject, String content);
}
