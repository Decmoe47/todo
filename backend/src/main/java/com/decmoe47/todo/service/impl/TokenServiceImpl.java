package com.decmoe47.todo.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.decmoe47.todo.config.property.SecurityProperties;
import com.decmoe47.todo.constant.JwtConstants;
import com.decmoe47.todo.constant.RedisConstants;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.model.vo.AuthenticationTokensVO;
import com.decmoe47.todo.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public AuthenticationTokensVO generate(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String accessToken = generateToken(user, securityProperties.getAccessTokenTimeToLive());
        String refreshToken = generateToken(user, securityProperties.getRefreshTokenTimeToLive());

        return new AuthenticationTokensVO()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
    }

    @Override
    public Authentication parse(String token) {
        JSONObject payloads = JWTUtil.parseToken(token).getPayloads();
        Long userId = payloads.getLong(JwtConstants.USER_ID);
        String email = payloads.getStr(JwtConstants.EMAIL);

        User user = new User().setId(userId).setEmail(email);
        return new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
    }

    @Override
    public boolean isValidated(String token) {
        if (!JWTUtil.verify(token, securityProperties.getSecretKey().getBytes())) {
            return false;
        }

        JWT jwt = JWTUtil.parseToken(token);
        return !redisTemplate.hasKey(StrUtil.format(RedisConstants.BLACKLIST_TOKEN, jwt.getPayload(JWTPayload.JWT_ID)));
    }

    @Override
    public AuthenticationTokensVO refresh(String refreshToken) {
        JSONObject payloads = JWTUtil.parseToken(refreshToken).getPayloads();
        Long userId = payloads.getLong(JwtConstants.USER_ID);
        String email = payloads.getStr(JwtConstants.EMAIL);

        User user = new User().setId(userId).setEmail(email);
        String accessToken = generateToken(user, securityProperties.getAccessTokenTimeToLive());

        return new AuthenticationTokensVO()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
    }

    @Override
    public void invalidate(String token) {
        JSONObject payloads = JWTUtil.parseToken(token).getPayloads();
        String jwtId = payloads.getStr(JWTPayload.JWT_ID);
        Integer expiresAt = payloads.getInt(JWTPayload.EXPIRES_AT);

        String blacklistTokenKey = RedisConstants.BLACKLIST_TOKEN + jwtId;

        if (expiresAt != null) {
            int currentTimeSeconds = Convert.toInt(System.currentTimeMillis() / 1000);
            if (expiresAt < currentTimeSeconds) {
                // Token已过期，直接返回
                return;
            }
            // 计算Token剩余时间，将其加入黑名单
            int expirationIn = expiresAt - currentTimeSeconds;
            redisTemplate.opsForValue().set(blacklistTokenKey, "", expirationIn, TimeUnit.SECONDS);
        } else {
            // 永不过期的Token永久加入黑名单
            redisTemplate.opsForValue().set(blacklistTokenKey, "");
        }
    }

    private String generateToken(User user, int ttl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.USER_ID, user.getId());
        claims.put(JwtConstants.EMAIL, user.getEmail());

        Date now = new Date();
        claims.put(JWTPayload.ISSUED_AT, now);

        // 设置过期时间 -1 表示永不过期
        if (ttl != -1) {
            Date expiresAt = DateUtil.offsetSecond(now, ttl);
            claims.put(JWTPayload.EXPIRES_AT, expiresAt);
        }
        claims.put(JWTPayload.SUBJECT, user.getUsername());
        claims.put(JWTPayload.JWT_ID, IdUtil.simpleUUID());

        return JWTUtil.createToken(claims, securityProperties.getSecretKey().getBytes());
    }
}
