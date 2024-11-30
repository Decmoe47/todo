package com.decmoe47.todo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.decmoe47.todo.constant.MailTemplate;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.dto.UserLoginDTO;
import com.decmoe47.todo.model.dto.UserRegisterDTO;
import com.decmoe47.todo.model.dto.UserSearchDTO;
import com.decmoe47.todo.model.dto.UserUpdateDTO;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.model.vo.UserVO;
import com.decmoe47.todo.repository.UserRepository;
import com.decmoe47.todo.service.MailService;
import com.decmoe47.todo.service.UserService;
import com.decmoe47.todo.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final MailService mailService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepo;
    private final VerificationCodeService verificationCodeService;

    @Transactional(rollbackFor = Exception.class)
    public UserVO login(UserLoginDTO userLoginDTO, String loginIp) {
        Authentication authentication = authenticate(userLoginDTO.getEmail(), userLoginDTO.getPassword(), loginIp);
        User user = (User) authentication.getPrincipal();

        return BeanUtil.toBean(user, UserVO.class);
    }

    public UserVO register(UserRegisterDTO userRegisterDTO) {
        if (userRepo.findByEmail(userRegisterDTO.getEmail()).isPresent())
            throw new ErrorResponseException(ErrorCodeEnum.USER_ALREADY_EXISTS);

        verificationCodeService.checkCode(userRegisterDTO.getVerificationCode());

        User user = BeanUtil.toBean(userRegisterDTO, User.class);
        user = saveUser(user);

        return BeanUtil.toBean(user, UserVO.class);
    }

    public void sendVerifyCode(String email) throws ErrorResponseException {
        String code = verificationCodeService.createCode();
        List<String> toList = List.of(email);
        boolean sent = mailService.send(
                toList, MailTemplate.VERIFY_CODE_SUBJECT, MailTemplate.VERIFY_CODE_BODY.replace("{code}", code));
        if (!sent)
            throw new ErrorResponseException(ErrorCodeEnum.VERIFY_CODE_SEND_FAILED);
    }

    public UserVO getUser(int userId) throws ErrorResponseException {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
        return BeanUtil.toBean(user, UserVO.class);
    }

    public List<UserVO> searchUser(UserSearchDTO userSearchDTO) throws ErrorResponseException {
        if (userSearchDTO.getId() != null) {
            return List.of(getUser(userSearchDTO.getId()));
        }

        if (userSearchDTO.getEmail() != null) {
            List<User> users = userRepo.findByEmailContaining(userSearchDTO.getEmail())
                    .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
            return users.stream().map(e -> BeanUtil.toBean(e, UserVO.class)).toList();
        }

        if (userSearchDTO.getName() != null) {
            List<User> users = userRepo.findByUsernameContaining(userSearchDTO.getName())
                    .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
            return users.stream().map(e -> BeanUtil.toBean(e, UserVO.class)).toList();
        }

        throw new ErrorResponseException(ErrorCodeEnum.NO_QUERY_PARAM_PROVIDED);
    }

    public UserVO updateUser(int userId, UserUpdateDTO userUpdateDTO) throws ErrorResponseException {
        User user = userRepo.findById(userId).orElseThrow();

        if (CharSequenceUtil.isEmpty(userUpdateDTO.getName())) {
            user.setName(userUpdateDTO.getName());
        }
        if (CharSequenceUtil.isEmpty(userUpdateDTO.getNewEmail())) {
            verificationCodeService.checkCode(userUpdateDTO.getVerificationCode());
            user.setEmail(userUpdateDTO.getNewEmail());
        }

        userRepo.save(user);
        return BeanUtil.toBean(user, UserVO.class);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        user.setCredentialExpireTime(LocalDateTime.now().plusDays(30));
        return user;
    }

    public User saveUser(User user) {
        passwordHandle(user);
        return userRepo.save(user);
    }

    private Authentication authenticate(String email, String password, String clientIp) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(token);
        User user = (User) authenticate.getPrincipal();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plusDays = now.plusDays(7);
        user.setPassword(null).setLastLoginTime(now).setCredentialExpireTime(plusDays).setLastLoginIp(clientIp);
        User userToUpdate = new User().setLastLoginTime(now).setCredentialExpireTime(plusDays).setLastLoginIp(clientIp);
        userRepo.save(userToUpdate);
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        return authenticate;
    }

    private void passwordHandle(User user) {
        if (CharSequenceUtil.isNotEmpty(user.getPassword())) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
    }
}
