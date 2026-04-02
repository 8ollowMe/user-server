package com.followme.userserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            
            // 1. 세션을 사용하지 않고(Stateless), 오직 JWT 토큰으로만 인증하도록 설정합니다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 2. 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/users/register").permitAll()
                .anyRequest().authenticated()
            )
            
            // 3. OAuth2 리소스 서버 (JWT 검증) 기능 활성화!
            // 토큰의 유효 기간, 위조 여부를 검사.
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}