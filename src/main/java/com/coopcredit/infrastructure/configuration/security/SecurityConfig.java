package com.coopcredit.infrastructure.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/risk-evaluation").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // If swagger exists
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll() // Frontend resources

                        // Affiliates - Creation/Update only by internal staff
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/afiliados")
                        .hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/afiliados/**")
                        .hasAnyRole("ADMIN", "ANALISTA")
                        // Reading affiliates allowed for all roles (including AFILIADO viewing
                        // themselves)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/afiliados/**")
                        .hasAnyRole("ADMIN", "ANALISTA", "AFILIADO")

                        // Credit Applications - Only Affiliates can request
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/solicitudes")
                        .hasRole("AFILIADO")
                        // Evaluation endpoint - Only ANALISTA can evaluate pending applications
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/solicitudes/*/evaluar")
                        .hasRole("ANALISTA")
                        // Pending applications list - Only ANALISTA
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/solicitudes/pendientes")
                        .hasRole("ANALISTA")
                        // Viewing applications allowed for all authenticated (filtering by role in
                        // controller)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/solicitudes/**").authenticated()

                        // Admin User Management - Only ADMIN can manage user-affiliate links
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
