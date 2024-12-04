package com.decmoe47.todo.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.decmoe47.todo.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public boolean send(List<String> to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(ArrayUtil.toArray(to, String.class));
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
            log.info("邮件成功发送!");
            return true;
        } catch (MailException e) {
            log.error("发送邮件错误:", e);
            return false;
        }
    }

}