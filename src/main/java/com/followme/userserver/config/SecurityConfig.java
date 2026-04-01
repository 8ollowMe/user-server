package com.followme.userserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.followme.userserver.infrastructure.security.UserContextFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserContextFilter userContextFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 비활성화 (Stateless API 서버)
            .csrf(csrf -> csrf.disable()) 
            
            // 2. 세션 미사용
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. API 명세서 기반 권한 정밀 제어
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/error").permitAll() // Spring Boot 기본 에러 페이지는 인증 없이 접근 가능하도록 허용
                
                // [그룹 1] Public: 로그인 없이 접근 가능
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()

                // [그룹 2] Internal: 다른 마이크로서비스 서버 간 내부 호출용
                .requestMatchers("/internal/v1/users/**").permitAll()

                // [그룹 3] Me: 로그인한 일반 사용자 본인 제어
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").authenticated()

                // [그룹 4] Admin: MASTER 권한 전용 사용자 관리
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/status").hasRole("MASTER") // 승인/거절 처리
                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("MASTER")            // 전체 사용자 목록 검색
                .requestMatchers(HttpMethod.GET, "/api/v1/users/*").hasRole("MASTER")          // 특정 사용자 상세 조회
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*").hasRole("MASTER")        // 사용자 정보 수정
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/*").hasRole("MASTER")       // 사용자 계정 비활성화

                .anyRequest().authenticated()
            )
            
            .addFilterBefore(userContextFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}