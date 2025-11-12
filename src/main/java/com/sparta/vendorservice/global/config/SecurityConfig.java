package com.sparta.vendorservice.global.config;

import com.sparta.vendorservice.global.authz.InternalHeaderAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret.key}")
    private String internalSecret;

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> {
        });
        // 모든 요청 허용하되 내부 헤더가 없으면 401
        http.authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
        );

        // 필터 체인 앞단에서 내부 헤더 검증 및 Authentication 세팅
        http.addFilterBefore(new InternalHeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
