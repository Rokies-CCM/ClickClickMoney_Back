package com.click.click.security.jwt;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;


@Component
public class JwtTokenProvider {
    private final Key key;
    private final String issuer;
    private final long accessTokenValiditySeconds;


    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-validity-seconds}") long validity
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTokenValiditySeconds = validity;
    }


    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenValiditySeconds)))
                .signWith(key)
                .compact();
    }


    public String getUsername(String token) {
        return Jwts.parser().verifyWith((SecretKey) key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}