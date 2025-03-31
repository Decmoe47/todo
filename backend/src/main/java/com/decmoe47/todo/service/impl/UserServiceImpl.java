package com.decmoe47.todo.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.bestvike.linq.Linq;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.dto.UserSearchDTO;
import com.decmoe47.todo.model.dto.UserUpdateDTO;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.model.vo.UserVO;
import com.decmoe47.todo.repository.UserRepository;
import com.decmoe47.todo.service.UserService;
import com.decmoe47.todo.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final VerificationCodeService verificationCodeService;

    public UserVO getUser(long userId) throws ErrorResponseException {
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
            return Linq.of(users).select(e -> BeanUtil.toBean(e, UserVO.class)).toList();
        }

        if (userSearchDTO.getName() != null) {
            List<User> users = userRepo.findByNameContaining(userSearchDTO.getName())
                    .orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
            return Linq.of(users).select(e -> BeanUtil.toBean(e, UserVO.class)).toList();
        }

        throw new ErrorResponseException(ErrorCodeEnum.NO_QUERY_PARAM_PROVIDED);
    }

    public UserVO updateUser(long userId, UserUpdateDTO userUpdateDTO) throws ErrorResponseException {
        User user = userRepo.findById(userId).orElseThrow();

        if (StrUtil.isEmpty(userUpdateDTO.getName())) {
            user.setName(userUpdateDTO.getName());
        }
        if (StrUtil.isEmpty(userUpdateDTO.getNewEmail())) {
            verificationCodeService.checkCode(userUpdateDTO.getVerificationCode());
            user.setEmail(userUpdateDTO.getNewEmail());
        }

        userRepo.save(user);
        return BeanUtil.toBean(user, UserVO.class);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepo.findByEmail(email).orElseThrow();
    }
}
