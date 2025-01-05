package payup.payup.config;

import payup.payup.jwt.JwtRequestFilter;
import payup.payup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *  configures Spring Security for the PayUp application. 
 * sets up authentication and authorization rules, manages sessions, 
 *  implements JWT-based authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * Configures the security filter chain for HTTP requests.
     * 
     * @param http The HttpSecurity object to configure.
     * @return SecurityFilterChain for the configured security settings.
     * @throws Exception If there's an error during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection since we're using JWT which is stateless
            .csrf().disable()
            // Define which URLs are secured and what roles can access them
            .authorizeHttpRequests(auth -> auth
                // Allow registration and login without authentication
                .antMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // Admin routes require the ADMIN role
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // Landlord routes require the LANDLORD role
                .antMatchers("/api/landlord/**").hasRole("LANDLORD")
                // Tenant routes require the TENANT role
                .antMatchers("/api/tenant/**").hasRole("TENANT")
                // Any other request must be authenticated
                .anyRequest().authenticated()
            )
            // Set session management to stateless since we use JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Add our custom JWT request filter before Spring's UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates a bean for the AuthenticationManager which uses our UserService 
     * for user details and BCrypt for password validation.
     * 
     * @param http HttpSecurity for getting the shared AuthenticationManagerBuilder.
     * @return Customized AuthenticationManager.
     * @throws Exception If there's an error during bean creation.
     */
    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        // Use UserService for retrieving user details and BCrypt for password encoding
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
        return auth.build();
    }

    /**
     * Provides a bean for BCryptPasswordEncoder, which is used for hashing passwords.
     * 
     * @return A BCryptPasswordEncoder instance for secure password storage.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}