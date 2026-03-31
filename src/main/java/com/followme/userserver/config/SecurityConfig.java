package com.followme.userserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 비활성화 (Stateless API 서버)
            .csrf(csrf -> csrf.disable()) 
            
            // 2. 세션 미사용
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. API 명세서 기반 권한 정밀 제어
            .authorizeHttpRequests(auth -> auth
                // [그룹 1] Public: 로그인 없이 접근 가능
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()

                // [그룹 2] Internal: 다른 마이크로서비스 서버 간 내부 호출용
                .requestMatchers("/internal/v1/users/**").permitAll()

                // [그룹 3] Me: 로그인한 일반 사용자 본인 제어
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").authenticated()

                // [그룹 4] Admin: MASTER 권한 전용 사용자 관리 (명세서 순서대로 나열)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/status").hasRole("MASTER") // 승인/거절 처리
                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("MASTER")            // 전체 사용자 목록 검색
                .requestMatchers(HttpMethod.GET, "/api/v1/users/*").hasRole("MASTER")          // 특정 사용자 상세 조회
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*").hasRole("MASTER")        // 사용자 정보 수정
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/*").hasRole("MASTER")       // 사용자 계정 비활성화

                // [그 외] 혹시 빼먹은 요청이 있다면 무조건 인증을 요구하여 1차 방어
                .anyRequest().authenticated()
            )
            
            // 🌟 4. [핵심 변경 사항] 기본 설정 대신 Keycloak 전용 토큰 번역기를 달아줍니다!
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> 
                jwt.jwtAuthenticationConverter(keycloakJwtConverter())
            ));

        return http.build();
    }

    /**
     * 💡 Keycloak 토큰의 realm_access -> roles 안에 있는 권한을 꺼내서
     * 스프링 시큐리티가 알아먹을 수 있는 "ROLE_MASTER" 형태로 번역해 주는 메서드
     */
    private Converter<Jwt, AbstractAuthenticationToken> keycloakJwtConverter() {
        return jwt -> {
            // 1. 스프링의 기본 권한 추출기 동작 (scope 등 추출)
            JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> authorities = new ArrayList<>(defaultConverter.convert(jwt));
            
            // 2. Keycloak 전용 데이터(realm_access) 파싱
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                
                // 3. 추출한 "MASTER" 앞에 "ROLE_"을 붙여서 스프링 시큐리티에 등록
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
            
            // 4. 완성된 권한 목록을 스프링 시큐리티 컨텍스트에 전달
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}