package com.example.gym.security;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && tokenProvider.validateToken(jwt)) {
                Long userId = tokenProvider.extractUserId(jwt);
                String email = tokenProvider.extractEmail(jwt);
                String role = tokenProvider.extractRole(jwt);
                Long tokenVersion = tokenProvider.extractTokenVersion(jwt);
                
                // Fetch user from DB to check status and token version
                User dbUser = userRepository.findById(userId).orElse(null);
                
                Long dbTokenVersion = (dbUser != null && dbUser.getTokenVersion() != null) ? dbUser.getTokenVersion() : 0L;
                Long jwtTokenVersion = tokenVersion != null ? tokenVersion : 0L;

                if (dbUser != null && "ACTIVE".equalsIgnoreCase(dbUser.getStatus()) && dbTokenVersion.equals(jwtTokenVersion)) {
                    // Create a lightweight user representation from token claims
                    User virtualUser = new User();
                    virtualUser.setId(userId);
                    virtualUser.setEmail(email);
                    virtualUser.setRole(role);
                
                String safeRole = (role != null) ? role.toUpperCase() : "USER";
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        virtualUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + safeRole))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
