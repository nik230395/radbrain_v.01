package org.nikolic.programm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String requestUri = request.getRequestURI();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtUtil.isValidToken(token)) {
                    String email = jwtUtil.getEmailFromToken(token);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        Optional<User> userOpt = userRepository.findByEmail(email);

                        if (userOpt.isPresent()) {
                            User user = userOpt.get();

                            if (user.isActive() && user.isEmailVerified()) {
                                String role = "ROLE_" + user.getRole().name();
                                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                email,
                                                null,
                                                Collections.singletonList(authority)
                                        );

                                authentication.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request)
                                );

                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                logger.debug("Successfully authenticated admin/user: {}", email);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("JWT Auth error: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // WICHTIG: Admin-Pfade NIEMALS überspringen (muss false sein)
        if (path.startsWith("/api/admin/") || path.startsWith("/api/secure/")) {
            return false;
        }

        // Nur rein öffentliche Pfade überspringen
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/users/register") ||
                path.startsWith("/api/users/verify") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.equals("/");
    }
}