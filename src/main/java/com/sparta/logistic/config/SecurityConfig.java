package com.sparta.logistic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // disable CSRF for API endpoints (clients using Basic/Auth tokens won't send CSRF tokens)
            .csrf(csrf -> csrf.disable())

            // require authentication for the enquiry POST endpoint, allow other requests
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/logistic/enquiry").authenticated()
                    .requestMatchers("/api/mail/send").permitAll()
                .anyRequest().permitAll()
            )

            // enable HTTP Basic auth
            .httpBasic(withDefaults());

        return http.build();
    }

    // dev-friendly plain-text encoder so the password in application.properties works as-is
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}

