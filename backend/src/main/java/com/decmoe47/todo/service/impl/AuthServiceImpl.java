package com.decmoe47.todo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.decmoe47.todo.constant.MailTemplate;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.dto.UserLoginDTO;
import com.decmoe47.todo.model.dto.UserRegisterDTO;
import com.decmoe47.todo.model.entity.TodoList;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.model.vo.AuthenticationTokensVO;
import com.decmoe47.todo.model.vo.UserVO;
import com.decmoe47.todo.repository.TodoListRepository;
import com.decmoe47.todo.repository.UserRepository;
import com.decmoe47.todo.service.AuthService;
import com.decmoe47.todo.service.MailService;
import com.decmoe47.todo.service.TokenService;
import com.decmoe47.todo.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final MailService mailService;
    private final TokenService tokenService;
    private final UserRepository userRepo;
    private final TodoListRepository todoListRepo;
    private final VerificationCodeService verificationCodeService;
    private final AuthenticationManager authenticationManager;

    @Transactional(rollbackFor = Exception.class)
    public UserVO login(UserLoginDTO userLoginDTO) {
        Authentication authentication = authenticate(userLoginDTO.getEmail(), userLoginDTO.getPassword());
        User user = (User) authentication.getPrincipal();
        UserVO userVO = BeanUtil.toBean(user, UserVO.class);

        AuthenticationTokensVO authenticationTokensVO = tokenService.generate(authentication);
        userVO.setTokens(authenticationTokensVO);

        return userVO;
    }

    public UserVO register(UserRegisterDTO userRegisterDTO) {
        if (userRepo.findByEmail(userRegisterDTO.getEmail()).isPresent())
            throw new ErrorResponseException(ErrorCodeEnum.USER_ALREADY_EXISTS);

        verificationCodeService.checkCode(userRegisterDTO.getVerificationCode(), userRegisterDTO.getEmail());

        User user = BeanUtil.toBean(userRegisterDTO, User.class);
        user = saveNewUser(user);

        createInboxTodoList(user);

        return BeanUtil.toBean(user, UserVO.class);
    }

    public void sendVerificationCode(String email) {
        String code = verificationCodeService.createCode(email);
        List<String> toList = List.of(email);
        boolean sent = mailService.send(
                toList, MailTemplate.VERIFICATION_CODE_SUBJECT,
                MailTemplate.VERIFICATION_CODE_BODY.replace("{code}", code));
        if (!sent)
            throw new ErrorResponseException(ErrorCodeEnum.VERIFICATION_CODE_SEND_FAILED);
    }

    @Override
    public void logout(String token) {
        tokenService.invalidate(token);
        SecurityContextHolder.clearContext();
    }

    @Override
    public AuthenticationTokensVO refreshAccessToken(String refreshToken) {
        if (!tokenService.isValidated(refreshToken)) {
            throw new ErrorResponseException(ErrorCodeEnum.REFRESH_TOKEN_EXPIRED);
        }
        return tokenService.refresh(refreshToken);
    }

    private Authentication authenticate(String email, String password) {
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authenticate = authenticationManager.authenticate(token);

            User user = (User) authenticate.getPrincipal();
            userRepo.save(user);

            SecurityContextHolder.getContext().setAuthentication(authenticate);

            return authenticate;
        } catch (BadCredentialsException e) {
            throw new ErrorResponseException(ErrorCodeEnum.USERNAME_OR_PASSWORD_INCORRECT, e);
        }

    }

    private User saveNewUser(User user) {
        if (CharSequenceUtil.isNotEmpty(user.getPassword())) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
        return userRepo.save(user);
    }

    private void createInboxTodoList(User user) {
        TodoList todoList = new TodoList();
        todoList.setInbox(true).setName("inbox").setCreatedBy(user);
        todoListRepo.save(todoList);
    }
}
