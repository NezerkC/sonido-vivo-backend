package cl.duoc.sonidovivo.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Stateless HMAC-SHA256 JWT token issuer and validator built with JJWT 0.12.x.
 */
@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;
    private static final String ROLES_CLAIM = "roles";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret must be configured");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes for HS256"
            );
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("app.jwt.expiration-ms must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationMs);
        List<String> roles = authorities == null
                ? List.of()
                : authorities.stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder()
                .subject(username)
                .claim(ROLES_CLAIM, roles)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object claim = extractAllClaims(token).get(ROLES_CLAIM);
        if (claim instanceof List<?> rawList) {
            return rawList.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        Date expiration = claims.getExpiration();
        return username != null
                && username.equals(userDetails.getUsername())
                && expiration != null
                && expiration.after(new Date());
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
