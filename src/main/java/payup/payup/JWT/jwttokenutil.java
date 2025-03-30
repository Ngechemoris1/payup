package payup.payup.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {
    private static final Logger LOGGER = Logger.getLogger(JwtTokenUtil.class.getName());

    @Value("${jwt.secret}")
    private String secret; // Changed from static to instance field

    @Value("${jwt.expiration}")
    private long jwtExpiration; // Changed from static to instance field

    // Instance method to get the signing key
    Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("jti", UUID.randomUUID().toString());
            // Add roles to the token claims
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            LOGGER.info("Generating token for user: " + userDetails.getUsername() + " with roles: " + claims.get("roles"));

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

            LOGGER.info("Token generated successfully");
            return token;
        } catch (Exception e) {
            LOGGER.severe("Error generating token: " + e.getMessage());
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> {
            List<?> roles = claims.get("roles", List.class);
            return roles != null ? roles.stream().map(Object::toString).collect(Collectors.toList()) : null;
        });
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            final List<String> tokenRoles = getRolesFromToken(token);
            boolean rolesMatch = tokenRoles != null && tokenRoles.containsAll(
                    userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList())
            );
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && rolesMatch);
        } catch (JwtException e) {
            LOGGER.warning("Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}