package org.home.paper.server.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.home.paper.server.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${token.signing.key}")
    private String signingKey;

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        var userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String generateToken(UserDetails userDetails) {
        var claims = new HashMap<String, Object>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("username", customUserDetails.getUsername());
        }

        return generateToken(claims, userDetails);
    }

    private String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        var expirationTime = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.UTC).toEpochMilli();
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(expirationTime))
                .signWith(getSigningKey())
                .compact();

    }

    private SecretKey getSigningKey() {
        var bytes = Decoders.BASE64.decode(signingKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        try {
            Claims claims = extractAllClaims(token);
            return claimsResolvers.apply(claims);
        } catch (ExpiredJwtException ex) {
            return claimsResolvers.apply(ex.getClaims());
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .decryptWith(getSigningKey())
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
