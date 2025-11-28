package com.apigateway.filter;

import com.apigateway.service.AnalyticsService;
import com.apigateway.service.RouteService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoutingFilter extends OncePerRequestFilter {

    private final RouteService routeService;
    private final RestTemplate restTemplate;
    private final AnalyticsService analyticsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // Do not route internal endpoints
        if (path.startsWith("/auth/") || path.startsWith("/admin/") || path.equals("/health") || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> dest = routeService.resolveServiceUrl(path);
        if (dest.isEmpty()) {
            analyticsService.incError("routing");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("No route configured");
            return;
        }

        String base = dest.get();
        String forwardUrl = base;
        if (base.endsWith("/")) {
            forwardUrl = base.substring(0, base.length() - 1);
        }
        forwardUrl = forwardUrl + path;
        if (request.getQueryString() != null) {
            forwardUrl += "?" + request.getQueryString();
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        } catch (Exception ex) {
            method = HttpMethod.GET;
        }

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String h = headerNames.nextElement();
            if (h.equalsIgnoreCase("host")) continue; // override
            headers.put(h, Collections.list(request.getHeaders(h)));
        }

        byte[] bodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
        HttpEntity<byte[]> entity = new HttpEntity<>(bodyBytes, headers);

        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(URI.create(forwardUrl), method, entity, byte[].class);
            // copy response
            response.setStatus(resp.getStatusCode().value());
            for (var e : resp.getHeaders().entrySet()) {
                for (String v : e.getValue()) {
                    response.addHeader(e.getKey(), v);
                }
            }
            if (resp.hasBody()) {
                response.getOutputStream().write(resp.getBody());
            }
            // analytics by service host
            analyticsService.incTrafficForService(base);
        } catch (Exception ex) {
            analyticsService.incError("routing");
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().write("Routing failed");
        }
    }
}
