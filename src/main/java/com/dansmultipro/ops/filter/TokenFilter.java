package com.dansmultipro.ops.filter;

import com.dansmultipro.ops.constant.RoleTypeConstant;
import com.dansmultipro.ops.dto.common.ErrorResDto;
import com.dansmultipro.ops.exception.BlacklistedTokenException;
import com.dansmultipro.ops.pojo.AuthorizationPOJO;
import com.dansmultipro.ops.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final List<RequestMatcher> antMatchers;
    private final ObjectMapper objectMapper;

    public TokenFilter(JwtUtil jwtUtil, List<RequestMatcher> antMatchers, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.antMatchers = antMatchers;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        boolean matched = antMatchers.stream()
                .anyMatch(requestMatcher -> requestMatcher.matches(request));
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (matched) {
                filterChain.doFilter(request, response);
                return;
            }
            writeJsonError(response, "Authentication required: please log in");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parseClaims(token);
            String userId = claims.get("userId", String.class);
            String roleValue = claims.get("role", String.class);

            RoleTypeConstant role = RoleTypeConstant.valueOf(roleValue);
            AuthorizationPOJO principal = new AuthorizationPOJO(userId, role);
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (BlacklistedTokenException ex) {
            writeJsonError(response, ex.getMessage());
        } catch (ExpiredJwtException ex) {
            writeJsonError(response, "Token has expired");
        } catch (JwtException ex) {
            writeJsonError(response, "Invalid token provided");
        } catch (Exception ex) {
            ex.printStackTrace();
            writeJsonError(response, "Unauthorized");
        }
    }

    private void writeJsonError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = objectMapper.writeValueAsString(new ErrorResDto<>(message));
        response.getWriter().write(json);
    }
}
