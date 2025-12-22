package org.nikolic.programm. security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework. security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto. bcrypt.BCryptPasswordEncoder;
import org.springframework.security. crypto.password.PasswordEncoder;
import org.springframework.security. web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Öffentliche Authentication-Endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ Öffentliche User-Registration-Endpoints
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/verify").permitAll()
                        .requestMatchers("/api/users/resend").permitAll()
                        .requestMatchers("/api/users/registration-status/**").permitAll()

                        // ✅ Öffentliche Quiz-Endpoints
                        .requestMatchers("/api/quizzes/published").permitAll()
                        .requestMatchers("/api/quizzes/*/submit").permitAll()
                        .requestMatchers("/api/quizzes/*").permitAll()  // GET einzelner Quiz

                        // ✅ Setup-Endpoints (falls vorhanden)
                        .requestMatchers("/api/setup/**").permitAll()

                        // ✅ Admin-Endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/secure/admin/**").hasRole("ADMIN")

                        // ✅ Geschützte User-Endpoints
                        .requestMatchers("/api/secure/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users/admin/**").hasRole("ADMIN")

                        // ✅ Statische Ressourcen (HTML, CSS, JS)
                        .requestMatchers(
                                "/",
                                "/*. html",
                                "/*.css",
                                "/*.js",
                                "/*.png",
                                "/*.jpg",
                                "/*.ico",
                                "/*.svg",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/verify.html",
                                "/dashboard.html"
                        ).permitAll()

                        // ✅ Alle anderen Requests authentifiziert
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Erlaubte Origins (für Development)
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowCredentials(true);

        // ✅ Erlaubte HTTP-Methods
        configuration.setAllowedMethods(List. of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // ✅ Erlaubte Headers
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Exposed Headers
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}