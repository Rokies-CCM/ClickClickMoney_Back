package com.click.click.security.config;

import com.click.click.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개(무인증)
                        .requestMatchers(
                                "/ai/**",
                                "/click/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/consumption/**",
                                "/budgets", "/budgets/**",
                                "/memo/**",
                                "/auth/**",
                                "/internal/**"        // 내부 서버-투-서버용(컨트롤러에서 키 검증)
                        ).permitAll()

                        // 미션 API는 반드시 인증 필요(명시적으로 선언)
                        .requestMatchers("/missions/**").authenticated()

                        // 그 외는 인증 필요 (예: /points/** 등)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
