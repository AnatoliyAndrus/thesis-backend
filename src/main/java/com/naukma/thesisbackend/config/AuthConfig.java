package com.naukma.thesisbackend.config;

import com.naukma.thesisbackend.auth.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class AuthConfig {

    private final SecurityFilter securityFilter;

    public AuthConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //temporary settings, to be deleted
        httpSecurity.csrf().disable();
        httpSecurity.headers().frameOptions().disable();


        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*", "/api/v1/users/{userId}/posts", "api/v1/users/{userId}/avatar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/profile", "/api/v1/users/{userId}/liked-posts").access(new WebExpressionAuthorizationManager("#userId == authentication.name"))
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/{userId}/avatar").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "api/v1/users/{userId}/avatar", "api/v1/users/{userId}").access(new WebExpressionAuthorizationManager("hasRole('ROLE_ADMIN') or #userId == authentication.name"))

                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").authenticated()
                        .requestMatchers("api/v1/posts/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "api/v1/comments/**").permitAll()
                        .requestMatchers("api/v1/comments/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v1/tags/**").permitAll()
                        .requestMatchers("/api/v1/tags/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/*").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}