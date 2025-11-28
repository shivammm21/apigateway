package com.apigateway.filter;

import com.apigateway.service.AnalyticsService;
import com.apigateway.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AnalyticsService analyticsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // Skip auth for internal endpoints
        if (path.startsWith("/auth/") || path.startsWith("/admin/") || path.equals("/health") || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        Map<String, Object> validation = jwtService.validate(auth);
        boolean valid = Boolean.TRUE.equals(validation.get("valid"));
        if (!valid) {
            // not strictly blocking here; services may enforce; but record analytics
            analyticsService.incError("jwt");
        } else {
            request.setAttribute("userId", validation.get("userId"));
            request.setAttribute("role", validation.get("role"));
            Object uid = validation.get("userId");
            if (uid != null) {
                analyticsService.incTrafficForUser(uid.toString());
            }
        }
        filterChain.doFilter(request, response);
    }
}
