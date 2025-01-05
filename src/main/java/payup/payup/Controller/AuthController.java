package payup.payup.controller;

import payup.payup.jwt.JwtTokenUtil;
import payup.payup.model.User;
import payup.payup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Endpoint for user registration. 
     * @param user User data to register.
     * @return ResponseEntity with the registered user details or an error.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid User user) {
        try {
            User registeredUser = userService.registerUser(user);
            LOGGER.info("User registered: " + user.getEmail());
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            LOGGER.warning("Registration failed for user: " + user.getEmail() + ". Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Endpoint for user login. Authenticates the user and issues a JWT if successful.
     * @param authRequest User credentials for authentication.
     * @param response HttpServletResponse to set the cookie.
     * @return ResponseEntity with authentication status or error.
     * @throws Exception If authentication fails, e.g., invalid credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User authRequest, HttpServletResponse response) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            LOGGER.warning("Login attempt failed for email: " + authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        UserDetails userDetails = userService.loadUserByUsername(authRequest.getEmail());
        String token = jwtTokenUtil.generateToken(userDetails);

        // Set JWT in HttpOnly cookie
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Only send the cookie over HTTPS
        cookie.setPath("/");
        response.addCookie(cookie);

        LOGGER.info("User logged in: " + authRequest.getEmail());
        return ResponseEntity.ok("Login successful");
    }
}