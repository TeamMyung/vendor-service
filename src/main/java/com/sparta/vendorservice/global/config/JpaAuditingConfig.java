package com.sparta.vendorservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
    /*
    @Bean
    public AuditorAware<Long> auditorAware() {
        // 로그인 기능 붙기 전까지는 임시값 1L 사용
        return () -> Optional.of(1L);
    }

    */
    @Bean
    public AuditorAware<String> auditorAware() {
        return new SecurityAuditorAware();
    }

    public static class SecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.isAuthenticated()) return Optional.empty();

            try {
                return Optional.of(auth.getName());
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
    }


}