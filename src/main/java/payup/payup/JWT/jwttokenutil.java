package payup.payup.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    /**
     * Generates a JWT token for the given user details.
     * @param userDetails User details to encode in the token.
     * @return The generated JWT token as a String.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", UUID.randomUUID().toString()); // JWT ID for token revocation
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates a JWT token with specified claims and a subject.
     * @param claims Additional claims to include in the token.
     * @param subject The subject (username) of the token.
     * @return The compact serialized JWT token.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                   .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                   .compact();
    }

    /**
     * Retrieves the username from a JWT token.
     * @param token The JWT token to parse.
     * @return The username from the token's subject.
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Retrieves any claim from the token using a claim resolver.
     * @param token The JWT token to parse.
     * @param claimsResolver Function to resolve the claim.
     * @param <T> The type of the claim to return.
     * @return The claim resolved by the claimsResolver function.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token and retrieves all claims.
     * @param token The JWT token to parse.
     * @return Claims object containing all claims in the token.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    /**
     * Validates the JWT token for the user.
     * @param token The JWT token to validate.
     * @param userDetails User details to match against the token.
     * @return true if the token is valid for the user, false otherwise.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Checks if the token has expired.
     * @param token The JWT token to check.
     * @return true if the token has expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return getClaimFromToken(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Checks if the token can be used based on the 'not before' claim.
     * @param token The JWT token to check.
     * @return true if the token can be used, false otherwise.
     */
    public Boolean canTokenBeUsed(String token) {
        return !getClaimFromToken(token, Claims::getNotBefore).after(new Date());
    }

    /**
     * Retrieves the expiration date from the token.
     * @param token The JWT token to parse.
     * @return The expiration date of the token.
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
}