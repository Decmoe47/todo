package com.decmoe47.todo.filter;

import cn.hutool.core.util.StrUtil;
import com.decmoe47.todo.constant.SecurityConstants;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.service.TokenService;
import com.decmoe47.todo.util.ResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain) throws ServletException, IOException {
        if (SecurityConstants.AUTH_WHITELIST.stream().anyMatch(s -> StrUtil.equals(s, request.getRequestURI()))) {
            chain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isEmpty(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            ResponseUtil.writeErrMsg(response, ErrorCodeEnum.UNAUTHORIZED);
            return;
        }
        String token = authorizationHeader.substring("Bearer ".length());

        if (!tokenService.isValidated(token)) {
            ResponseUtil.writeErrMsg(response, ErrorCodeEnum.ACCESS_TOKEN_EXPIRED);
            return;
        }

        Authentication authentication = tokenService.parse(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}
