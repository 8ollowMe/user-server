package com.followme.userserver.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-Role");

        if (StringUtils.hasText(userId) && StringUtils.hasText(role)) {
            try {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, "", Collections.singletonList(authority));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("User Context 설정 완료 - UserID: {}, Role: {}", userId, role);
            } catch (Exception e) {
                log.error("User Context 설정 중 오류 발생", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}