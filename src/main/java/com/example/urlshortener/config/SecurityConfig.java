package com.example.urlshortener.config;

import com.example.urlshortener.domain.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean UserDetailsService userDetailsService(AppUserRepository users) { return email -> users.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException("Account not found")); }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider::authenticate;
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/favicon.ico", "/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/*").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, ex) -> { response.setStatus(401); response.setContentType(MediaType.APPLICATION_JSON_VALUE); response.getWriter().write("{\"error\":\"Please sign in first\"}"); })
                .accessDeniedHandler((request, response, ex) -> { response.setStatus(403); response.setContentType(MediaType.APPLICATION_JSON_VALUE); response.getWriter().write("{\"error\":\"You do not have permission to do that\"}"); }))
            .build();
    }
}
