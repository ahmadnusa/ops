package com.dansmultipro.ops.config;

import com.dansmultipro.ops.filter.RateLimitFilter;
import com.dansmultipro.ops.filter.TokenFilter;
import com.dansmultipro.ops.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public List<RequestMatcher> getMatchers() {
        ArrayList<RequestMatcher> matchers = new ArrayList<>();
        matchers.add(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/users" +
                "/register"));
        matchers.add(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/users" +
                "/forgot-password"));
        matchers.add(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/auth/login"));
        matchers.add(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/v3/api-docs/**"));
        matchers.add(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/swagger-ui/**"));
        return matchers;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserService userService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenFilter tokenFilter,
            RateLimitFilter rateLimitFilter,
            AuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, TokenFilter.class);
        return http.build();
    }
}
