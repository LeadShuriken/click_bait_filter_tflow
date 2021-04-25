package com.clickbait.tflow.security;

import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;

import com.clickbait.tflow.config.JWTConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JWTUtils {

    final private JWTConfig jwtConfig;

    public JWTUtils(JWTConfig conf) {
        jwtConfig = conf;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String userNameP) {
        final String username = extractUsername(token);
        return (username.equals(userNameP) && !isTokenExpired(token));
    }
}
