package com.routes.routemanager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class UuidAuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String UUID_AUTHORITY = "UUID_AUTHORITY";

    @Value("${user.me.endpoint}")
    private String userMeEndpoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null) {
            handleNoAuthorizationHeader(response);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("Authorization header found: " + authorizationHeader);

        try {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            HttpStatusCode statusCode = isValidToken(token);

            // Validate the token by calling the user me endpoint
            if (statusCode == HttpStatus.OK || request.getRequestURI().equals("/routes/reset")) {
                SecurityContextHolder.getContext().setAuthentication(createAuthentication());
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(statusCode.value());
            }
        } catch (NullPointerException e) {
            System.err.println("Error processing authorization header: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleNoAuthorizationHeader(HttpServletResponse response) {
        System.err.println("No authorization header found");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    private HttpStatusCode isValidToken(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(userMeEndpoint, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            System.out.println("Response Body: " + responseBody);
            return response.getStatusCode();
        } catch (HttpClientErrorException e) {
            return e.getStatusCode();
        }
    }

    private UsernamePasswordAuthenticationToken createAuthentication() {
        return new UsernamePasswordAuthenticationToken(null, null, Collections.singleton(new SimpleGrantedAuthority(UUID_AUTHORITY)));
    }
}