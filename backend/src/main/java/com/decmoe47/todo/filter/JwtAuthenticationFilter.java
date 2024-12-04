package com.decmoe47.todo.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.alibaba.fastjson2.JSONObject;
import com.bestvike.linq.Linq;
import com.decmoe47.todo.constant.CommonConstants;
import com.decmoe47.todo.constant.JwtConstants;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.exception.AuthenticationException;
import com.decmoe47.todo.exception.ErrorResponseException;
import com.decmoe47.todo.model.entity.User;
import com.decmoe47.todo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final PathPatternParser parser = new PathPatternParser();
    private static final Map<String, PathPattern> patternCache = new HashMap<>();

    static {
        String[] patterns = CommonConstants.AUTH_WHITELIST;
        for (String patternStr : patterns) {
            patternCache.put(patternStr, parser.parse(patternStr));
        }
    }

    private final UserRepository userRepo;
    @Value("${base64Secret}")
    private String base64Secret;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain) throws ServletException, IOException {
        for (PathPattern pattern : patternCache.values()) {
            if (pattern.matches(PathContainer.parsePath(request.getRequestURI()))) {
                chain.doFilter(request, response);
                return;
            }
        }

        String token = getJwtFromRequest(request);
        if (token != null && JWTUtil.verify(token, base64Secret.getBytes())) {
            JWT jwt = JWTUtil.parseToken(token);
            long userId = ((Number) jwt.getPayload(JwtConstants.USER_ID)).longValue();

            User user = getUser(request, userId);
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, token,
                    user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private @Nullable String getJwtFromRequest(HttpServletRequest request) {
        Cookie cookie = Linq.of(request.getCookies()).where(c -> StrUtil.equals(c.getName(), JwtConstants.TOKEN)).first();
        return cookie.getValue() != null ? cookie.getValue() : null;
    }

    private User getUser(HttpServletRequest request, long userId) {
        HttpSession session = request.getSession(true);
        if (session == null) throw new AuthenticationException("session is null");

        User user = JSONObject.parseObject((String) session.getAttribute("user"), User.class);
        if (user == null) {
            user = userRepo.findById(userId).orElseThrow(() -> new ErrorResponseException(ErrorCodeEnum.USER_NOT_FOUND));
            session.setAttribute("user", JSONObject.toJSONString(user));
        }

        return user;
    }
}
