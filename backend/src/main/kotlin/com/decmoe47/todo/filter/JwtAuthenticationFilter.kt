package com.decmoe47.todo.filter

import com.decmoe47.todo.constant.JwtConstants
import com.decmoe47.todo.constant.SecurityConstants
import com.decmoe47.todo.constant.enums.ErrorCode
import com.decmoe47.todo.service.TokenService
import com.decmoe47.todo.util.writeErrMsg
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(private val tokenService: TokenService) : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (request.requestURI in SecurityConstants.AUTH_WHITELIST) {
            chain.doFilter(request, response)
            return
        }

        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(JwtConstants.BEARER_PREFIX) }
            ?.removePrefix(JwtConstants.BEARER_PREFIX)
            ?: run {
                response.writeErrMsg(ErrorCode.UNAUTHORIZED)
                return
            }

        if (!tokenService.isValid(token)) {
            response.writeErrMsg(ErrorCode.ACCESS_TOKEN_EXPIRED)
            return
        }

        SecurityContextHolder.getContext().authentication = tokenService.parse(token)

        chain.doFilter(request, response)
    }
}
