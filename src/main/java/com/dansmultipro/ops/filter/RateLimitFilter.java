package com.dansmultipro.ops.filter;

import com.dansmultipro.ops.dto.common.ErrorResDto;
import com.dansmultipro.ops.pojo.AuthorizationPOJO;
import com.dansmultipro.ops.util.RateLimitUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitUtil limiter;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitUtil limiter, ObjectMapper objectMapper) {
        this.limiter = limiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = buildKey(request);
        String uri = request.getRequestURI();
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed = limiter.allowRequest(key);
        if (allowed) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            String json = objectMapper.writeValueAsString(new ErrorResDto<>("Too many requests: please try again later"));
            response.getWriter().write(json);
        }
    }

    private boolean isWhitelisted(String uri) {
        return uri.startsWith("/actuator")
                || uri.startsWith("/docs")
                || uri.startsWith("/swagger")
                || uri.equals("/");
    }

    private String buildKey(HttpServletRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthorizationPOJO p) {
            return "user:" + p.id();
        }

        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = req.getRemoteAddr();
        }
        return "ip:" + ip;
    }
}

