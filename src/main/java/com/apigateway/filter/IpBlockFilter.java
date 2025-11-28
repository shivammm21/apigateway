package com.apigateway.filter;

import com.apigateway.service.IpBlockService;
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
public class IpBlockFilter extends OncePerRequestFilter {

    private final IpBlockService ipBlockService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ip = clientIp(request);
        if (ipBlockService.isBlocked(ip)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("IP blocked");
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
