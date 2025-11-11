package com.sparta.vendorservice.global.authz;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class InternalHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // 게이트웨이가 전달한 사용자 컨텍스트 추출
        String username = req.getHeader("username");
        String role = req.getHeader("role");

        // 권한 세팅
        Collection<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_" + (role == null ? "" : role)));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username == null ? "" : username, null, auths);

        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }
}
