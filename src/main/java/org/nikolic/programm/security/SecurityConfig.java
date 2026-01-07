package org.nikolic.programm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
        logger.info("🔐 SecurityConfig initialized");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("🔧 Configuring Security Filter Chain...");

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/verify.html",
                                "/forgot-password.html",
                                "/quiz.html",
                                "/roentgen.html",
                                "/ct.html",
                                "/mrt.html",
                                "/ultraschall.html",
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/*.png",
                                "/*.jpg",
                                "/*.jpeg",
                                "/*.gif",
                                "/*.ico",
                                "/*.svg",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/uploads/**"
                        ).permitAll()

                        // ============================================
                        // PUBLIC AUTHENTICATION ENDPOINTS
                        // ============================================
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-reset-code",
                                "/api/auth/reset-password",
                                "/api/auth/logout",
                                "/api/auth/validate"
                        ).permitAll()

                        // Test endpoints
                        .requestMatchers("/api/test/**").permitAll()

                        // /api/auth/me needs authentication
                        .requestMatchers("/api/auth/me").authenticated()

                        // ============================================
                        // PUBLIC USER REGISTRATION
                        // ============================================
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/verify",
                                "/api/users/resend",
                                "/api/users/registration-status/**"
                        ).permitAll()

                        // ============================================
                        // PUBLIC QUIZ ENDPOINTS
                        // ============================================
                        .requestMatchers(
                                "/api/quizzes/published",
                                "/api/quizzes/*/submit"
                        ).permitAll()
                        .requestMatchers("/api/quizzes/*").permitAll()

                        // ============================================
                        // ADMIN-ONLY API ENDPOINTS
                        // ============================================
                        .requestMatchers("/api/secure/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/questions/**").hasRole("ADMIN")
                        .requestMatchers("/api/choices/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/upload-image").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ============================================
                        // ADMIN-ONLY HTML PAGES
                        // ============================================
                        .requestMatchers(
                                "/quiz-creator.html",
                                "/admin-dashboard.html",
                                "/admin.html",
                                "/admin-panel.html"
                        ).hasRole("ADMIN")

                        // ============================================
                        // AUTHENTICATED USER PAGES
                        // ============================================
                        .requestMatchers(
                                "/user-home.html",
                                "/dashboard.html",
                                "/account.html"
                        ).authenticated()

                        // ============================================
                        // AUTHENTICATED API ENDPOINTS
                        // ============================================
                        .requestMatchers("/api/secure/**").authenticated()
                        .requestMatchers("/api/attempts/**").authenticated()

                        // ============================================
                        // DEFAULT: Allow all other requests (for development)
                        // ============================================
                        .anyRequest().permitAll()  // ✅ Changed from .authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        logger.info("✅ Security Filter Chain configured successfully");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.debug("🌐 Configuring CORS...");

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));


        configuration.setAllowCredentials(true);

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size"
        ));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        logger.info("CORS configured with allowedOriginPatterns");
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Creating BCrypt Password Encoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.debug("Creating Authentication Manager");
        return authenticationConfiguration.getAuthenticationManager();
    }
}