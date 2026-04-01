package com.followme.userserver;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class UserserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserserverApplication.class, args);
	}
	
	@Bean
    public AuditorAware<String> auditorAware() {
        // 임시로 "system" 이라는 이름을 사용
        return () -> Optional.of("system");
    }

}
