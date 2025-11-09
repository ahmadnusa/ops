package com.dansmultipro.ops.config;

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

import com.dansmultipro.ops.filter.TokenFilter;
import com.dansmultipro.ops.service.UserService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserService userService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
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
            AuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                // .exceptionHandling(handler -> handler
                // .authenticationEntryPoint((request, response, ex) -> {
                // response.setStatus(HttpStatus.UNAUTHORIZED.value());
                // response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                // response.getWriter().write("{\"message\":\"Unauthorized\"}");
                // }))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/users/approve/*").hasRole("SA")
                        .requestMatchers(HttpMethod.PUT, "/payments/*/approve").hasRole("GATEWAY")
                        .requestMatchers(HttpMethod.PUT, "/payments/*/reject").hasRole("GATEWAY")
                        .requestMatchers(HttpMethod.POST, "/payments").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/payments/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/payments/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/payments", "/payments/*")
                        .hasAnyRole("SA", "GATEWAY", "CUSTOMER")
                        .anyRequest().authenticated());
        return http.build();
    }
}
