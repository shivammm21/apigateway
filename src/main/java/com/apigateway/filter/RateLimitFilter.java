package com.apigateway.filter;

import com.apigateway.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // Skip for internal/admin endpoints
        if (path.startsWith("/auth/") || path.startsWith("/admin/") || path.equals("/health") || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        Object userIdAttr = request.getAttribute("userId");
        boolean allowed = rateLimitService.allowForIp(ip);
        if (userIdAttr != null) {
            allowed = allowed && rateLimitService.allowForUser(userIdAttr.toString());
        }
        if (!allowed) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest req) {
        String h = req.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) {
            return h.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
