package org.home.paper.server.configuration;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.home.paper.server.service.JwtService;
import org.home.paper.server.service.UserService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String HEADER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        var header = request.getHeader(HEADER_NAME);
        if (header == null || !header.startsWith(HEADER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        var jwt = header.substring(HEADER_PREFIX.length());
        String username;
        try {
            username = jwtService.extractUserName(jwt);
        } catch (JwtException _) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!username.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            var context = SecurityContextHolder.createEmptyContext();
            UserDetails userDetails;
            try {
                userDetails = userService.loadUserByUsername(username);
            } catch (UsernameNotFoundException _) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                filterChain.doFilter(request, response);
                return;
            }

            var token = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(token);
            SecurityContextHolder.setContext(context);
        }
        filterChain.doFilter(request, response);
    }
}
