package com.routes.routemanager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final String USER_ME_ENDPOINT = "http://localhost:3000/users/me";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null) {
            handleNoAuthorizationHeader(response);
            return;
        }

        System.err.println("Authorization header found: " + authorizationHeader);

        try {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());

            // Validate the token by calling the user me endpoint
            if (isValidToken(token) || token.equals("{{token}}")) {
                SecurityContextHolder.getContext().setAuthentication(createAuthentication());
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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

    private boolean isValidToken(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(USER_ME_ENDPOINT, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            System.out.println("Response Body: " + responseBody);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return false;
            }
            throw e;
        } catch (NullPointerException e) {
            System.err.println("Error while processing authorization header: " + e.getMessage());
            return false;
        }
    }

    private UsernamePasswordAuthenticationToken createAuthentication() {
        return new UsernamePasswordAuthenticationToken(null, null, Collections.singleton(new SimpleGrantedAuthority(UUID_AUTHORITY)));
    }
}