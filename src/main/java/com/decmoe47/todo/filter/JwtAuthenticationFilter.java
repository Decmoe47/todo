package com.decmoe47.todo.filter;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.jwt.JWTUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.decmoe47.todo.constant.Constants;
import com.decmoe47.todo.constant.SessionAttributeKeys;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.model.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain) throws ServletException, IOException {
        if (ArrayUtil.contains(Constants.AUTH_WHITELIST, request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");
        String jwtToken = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            jwtToken = bearerToken.substring("Bearer ".length());
        }

        if (StringUtils.hasText(jwtToken)
                && JWTUtil.verify(jwtToken, Constants.BASE64_SECRET.getBytes(StandardCharsets.UTF_8))) {
            String userId = String.valueOf(JWTUtil.parseToken(jwtToken).getPayload(SessionAttributeKeys.USER_ID));
            // 从redis中读取缓存的用户信息
            JSONObject jsonObject = JSON.parseObject(SessionAttributeKeys.USER_ID);
            assert jsonObject != null;
            User user = jsonObject.toJavaObject(User.class);

            if (user == null || LocalDateTime.now().isAfter(user.getCredentialExpireTime())) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/json; charset=utf-8");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                JSONObject returnObj = new JSONObject();
                returnObj.put("code", ErrorCodeEnum.UNAUTHORIZED.name());
                returnObj.put("message", ErrorCodeEnum.UNAUTHORIZED.getMessage());
                PrintWriter writer = response.getWriter();
                writer.write(returnObj.toJSONString());
                writer.flush();
                return;
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, jwtToken,
                    user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
