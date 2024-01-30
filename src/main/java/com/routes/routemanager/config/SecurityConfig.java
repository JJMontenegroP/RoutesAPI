package com.routes.routemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).cors(AbstractHttpConfigurer::disable).authorizeHttpRequests(request -> request.requestMatchers("/routes/ping").permitAll()).addFilterBefore(uuidAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class).authorizeHttpRequests(request -> {
            request.requestMatchers("/routes").hasAnyAuthority("UUID_AUTHORITY").requestMatchers("/routes/*").hasAnyAuthority("UUID_AUTHORITY");
        }).build();
    }

    @Bean
    public UUIDAuthorizationFilter uuidAuthorizationFilter() {
        return new UUIDAuthorizationFilter();
    }
}