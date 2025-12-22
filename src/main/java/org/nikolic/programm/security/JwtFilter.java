package org.nikolic. programm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nikolic.programm.entities.User;
import org.nikolic. programm.repositories.UserRepository;
import org. springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
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

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader. substring(7);

            try {
                if (jwtUtil.isValidToken(token)) {
                    String email = jwtUtil.getEmailFromToken(token);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        Optional<User> userOpt = userRepository.findByEmail(email);

                        if (userOpt.isPresent()) {
                            User user = userOpt.get();

                            // Check if user is still active and verified
                            if (user.isActive() && user.isEmailVerified()) {

                                // ✅ Korrigierte Authority-Erstellung
                                List<SimpleGrantedAuthority> authorities = createAuthorities(user);

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                logger.debug("JWT authentication successful for user: {}", email);
                            } else {
                                logger.warn("Authentication failed - user inactive or unverified:  {}", email);
                            }
                        } else {
                            logger.warn("Authentication failed - user not found: {}", email);
                        }
                    }
                } else {
                    logger.debug("Invalid JWT token");
                }
            } catch (Exception e) {
                logger. error("JWT authentication error: ", e);
                // Clear any existing authentication
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Erstellt Authorities basierend auf User-Rolle
     */
    private List<SimpleGrantedAuthority> createAuthorities(User user) {
        if (user.getRole() == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // ✅ Korrigierte Konvertierung:  UserRole -> String -> Authority
        String roleName = "ROLE_" + user.getRole().toString();
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Öffentliche Endpoints, die keine Authentifizierung benötigen
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.equals("/api/quizzes/published") ||
                path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path. startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(". png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".ico") ||
                path.equals("/") ||
                path.equals("/favicon.ico");
    }
}