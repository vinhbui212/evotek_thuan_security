package org.example.thuan_security.config;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.model.Roles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class JwtTokenProvider {

    private String keyStore = "keystore.jks";
    private String password = "123456";
    private String alias = "vinhkey1";
    private KeyPair keyPair;
    private final int EXPIRATION_TIME = 300000;

    public JwtTokenProvider() {
        keyPair = keyPair(keyStore, password, alias);
    }

    private KeyPair keyPair(String keyStore, String keyStorePassword, String alias) {
        try {
            ClassPathResource resource = new ClassPathResource(keyStore);
            if (!resource.exists()) {
                log.error("Keystore file [{}] does not exist in the classpath", keyStore);
                throw new IllegalStateException("Keystore file cannot be found");
            }

            KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, keyStorePassword.toCharArray());
            return keyStoreKeyFactory.getKeyPair(alias);
        } catch (IllegalStateException e) {
            log.error("Cannot load keys from store: {}", e.getMessage(), e);
            throw e;
        }
    }



    public String createToken(Authentication authentication, String email) {
        long now = Instant.now().toEpochMilli();
        Date validity = new Date(now + EXPIRATION_TIME);

        List<String> roles=authentication.getAuthorities().stream().map(
                item->item.getAuthority()).toList();
        String role= roles.get(0);
        log.info(role);
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("email", email)
                .claim("role", role)
                .signWith(SignatureAlgorithm.RS256, keyPair.getPrivate())
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .compact();
    }

    public boolean validateToken(String authToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(keyPair.getPublic())
                    .parseClaimsJws(authToken)
                    .getBody();

            log.info("Claims: {}", claims);

            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage());
        }
        return false;
    }

    public String extractClaims(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(keyPair.getPublic())
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public LocalDateTime extractExpiration(String token) {
        Claims claims= Jwts.parser()
                .setSigningKey(keyPair.getPublic())
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

    public String extractRole(String token) {
        Claims claims=Jwts.parser()
                .setSigningKey(keyPair.getPublic())
                .parseClaimsJws(token)
                .getBody();
                return claims.get("role", String.class);
    }
}