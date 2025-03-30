package payup.payup.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import payup.payup.service.UserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    private static final List<String> ALLOWED_ALGORITHMS = Arrays.asList("HS512");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                String alg = Jwts.parserBuilder()
                        .setSigningKey(jwtTokenUtil.getSigningKey())
                        .build()
                        .parseClaimsJws(jwtToken)
                        .getHeader()
                        .getAlgorithm();

                if (!ALLOWED_ALGORITHMS.contains(alg)) {
                    throw new IllegalArgumentException("Invalid signing algorithm");
                }

                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException | ExpiredJwtException | MalformedJwtException |
                     UnsupportedJwtException | SignatureException e) {
                logger.warn("JWT validation failed: " + e.getMessage());
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                // Extract roles from token instead of relying solely on UserDetails
                List<String> roles = jwtTokenUtil.getRolesFromToken(jwtToken);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authenticated user: " + username + " with roles: " + roles);
            }
        }

        chain.doFilter(request, response);
    }
}