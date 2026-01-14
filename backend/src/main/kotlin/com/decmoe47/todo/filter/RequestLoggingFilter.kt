package com.decmoe47.todo.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

private val log = KotlinLogging.logger {}

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val req = ContentCachingRequestWrapper(request)
        val res = ContentCachingResponseWrapper(response)

        try {
            chain.doFilter(req, res)
        } finally {
            val reqBody = if (req.contentType == MediaType.APPLICATION_JSON_VALUE)
                req.contentAsByteArray.toString(Charsets.UTF_8).take(2000)
            else "(not a json body)"
            val reqQueryString = if (req.queryString != null) "?${req.queryString}" else ""

            val resBody = res.contentAsByteArray.toString(Charsets.UTF_8).take(2000)

            log.info {
                "[Request] ${request.method} ${request.requestURI}$reqQueryString $reqBody | [Response] ${res.status} $resBody"
            }

            res.copyBodyToResponse()
        }
    }
}