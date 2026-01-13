package com.decmoe47.todo.config

import com.decmoe47.todo.constant.SecurityConstants
import com.decmoe47.todo.filter.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration(proxyBeanMethods = false)
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .csrf { obj: CsrfConfigurer<HttpSecurity?>? -> obj!!.disable() }
            .cors(Customizer.withDefaults<CorsConfigurer<HttpSecurity?>?>())
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(*SecurityConstants.AUTH_WHITELIST.toTypedArray()).permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .rememberMe(Customizer.withDefaults<RememberMeConfigurer<HttpSecurity?>?>())

        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager? {
        return configuration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
