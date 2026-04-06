package com.followme.userserver.config;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = {"com.followme.userserver", "com.followMe.common"})
@EnableJpaRepositories(basePackages = {"com.followme.userserver", "com.followMe.common"})
public class JpaAuditConfig {

    private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return Optional.of(SYSTEM_UUID);
            }
            try {
                return Optional.of(UUID.fromString(auth.getName()));
            } catch (IllegalArgumentException e) {
                return Optional.of(SYSTEM_UUID);
            }
        };
    }
}