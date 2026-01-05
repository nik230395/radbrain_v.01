package org.nikolic.programm. security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org. springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        // Skip OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String requestUri = request.getRequestURI();

        logger.debug("🔍 Processing request: {} {}", request.getMethod(), requestUri);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtUtil.isValidToken(token)) {
                    String email = jwtUtil.getEmailFromToken(token);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        Optional<User> userOpt = userRepository.findByEmail(email);

                        if (userOpt.isPresent()) {
                            User user = userOpt. get();

                            // Verify user is active and verified
                            if (user.isActive() && user.isEmailVerified()) {

                                String role = "ROLE_" + user.getRole().name();
                                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                                logger.info("Authenticating user: {} with authority: {}", email, role);

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                email,
                                                null,
                                                Collections.singletonList(authority)
                                        );

                                authentication. setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request)
                                );

                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                logger.debug("Authentication successful for:  {}", email);
                            } else {
                                logger.warn("User inactive or unverified: {}", email);
                            }
                        } else {
                            logger.warn("User not found: {}", email);
                        }
                    }
                } else {
                    logger.debug("Invalid token");
                }
            } catch (Exception e) {
                logger.error("JWT Authentication error:  {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("No Bearer token found for:  {}", requestUri);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean shouldSkip = "OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/api/users/register") ||
                path.startsWith("/api/users/verify") ||
                path.startsWith("/api/users/resend") ||
                path.equals("/api/quizzes/published") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path. endsWith(".js") ||
                path.endsWith(".ico") ||
                path.equals("/");

        if (shouldSkip) {
            logger.debug("⏩ Skipping JWT filter for: {}", path);
        }

        return shouldSkip;
    }
}