package com.followme.userserver;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@EnableJpaAuditing
@SpringBootApplication
public class UserserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserserverApplication.class, args);
	}
	
	@Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            // 1. 현재 요청을 보낸 사용자의 인증 정보(SecurityContext)를 꺼내옵니다.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 2. 인증 정보가 없거나, 익명 사용자(회원가입 등 토큰 없이 온 사람)일 경우
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.of("system"); // 현재는 기본값으로 system으로 기록
            }

            // 3. 정상적으로 토큰을 들고 온 유저라면, 토큰 안에 있는 유저 ID(이름)를 반환합니다!
            return Optional.of(authentication.getName());
        };
    }
}
