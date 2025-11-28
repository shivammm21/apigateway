package com.apigateway.config;

import com.apigateway.filter.AuthenticationFilter;
import com.apigateway.filter.IpBlockFilter;
import com.apigateway.filter.LoggingFilter;
import com.apigateway.filter.RateLimitFilter;
import com.apigateway.filter.RoutingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationFilter authenticationFilter,
                                                   RateLimitFilter rateLimitFilter,
                                                   IpBlockFilter ipBlockFilter,
                                                   LoggingFilter loggingFilter,
                                                   RoutingFilter routingFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/health", 
                                "/actuator/**",
                                "/auth/validate",
                                "/admin/**" // In production, restrict admin endpoints appropriately
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        // Filter order: IP block -> Auth -> Rate limit -> Logging -> Routing
        http.addFilterBefore(ipBlockFilter, BasicAuthenticationFilter.class);
        http.addFilterAfter(authenticationFilter, IpBlockFilter.class);
        http.addFilterAfter(rateLimitFilter, AuthenticationFilter.class);
        http.addFilterAfter(loggingFilter, RateLimitFilter.class);
        http.addFilterAfter(routingFilter, LoggingFilter.class);

        return http.build();
    }
}
