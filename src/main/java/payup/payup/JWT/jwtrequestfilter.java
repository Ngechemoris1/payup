package payup.payup.jwt;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    // Allowed algorithms, this can be expanded if needed
    private static final List<String> ALLOWED_ALGORITHMS = Arrays.asList("HS512");

    /**
     * This method intercepts HTTP requests to validate JWT tokens.
     * 
     * @param request The HTTP request to process
     * @param response The HTTP response to send back
     * @param chain The filter chain for the request
     * @throws ServletException if an error occurs during filtering
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                // Validate the signing algorithm used in the token
                String alg = Jwts.parser().setSigningKey(jwtTokenUtil.getSecretKey())
                        .parseClaimsJws(jwtToken).getHeader().getAlgorithm();
                if (!ALLOWED_ALGORITHMS.contains(alg)) {
                    throw new IllegalArgumentException("Invalid signing algorithm");
                }

                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException | ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException e) {
                logger.warn("JWT validation failed: " + e.getMessage());
                // Optionally, you might want to send an error response here
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String");
        }

        // If a username was extracted from the token and there's no current authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userService.loadUserByUsername(username);

            // Validate the token's details against the user's details
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                // If token is valid, create a Spring Security authentication
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }
}