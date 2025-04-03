package payup.payup.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.*;
import payup.payup.jwt.JwtTokenUtil;
import payup.payup.mapper.UserMapper;
import payup.payup.model.User;
import payup.payup.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling authentication operations including
 * user registration and login. Uses JWT for authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    /**
     * Registers a new user in the system.
     *
     * @param userDto User registration data
     * @return ResponseEntity with registered UserDto or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserDto userDto) {
        try {
            logger.info("Attempting to register user with email: {}", userDto.getEmail());

            User user = userMapper.toEntity(userDto);
            User registeredUser = userService.registerUser(user);
            UserDto responseDto = userMapper.toDto(registeredUser);

            logger.info("User registered successfully: {}", userDto.getEmail());
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            logger.error("Registration failed for email {}: {}", userDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Registration failed", e.getMessage()));
        }
    }

    /**
     * Authenticates a user and issues JWT token upon successful login.
     *
     * @param loginRequest User credentials (email and password)
     * @param response HttpServletResponse for setting secure cookies
     * @return ResponseEntity with authentication details or error
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest,
                                   HttpServletResponse response) {
        try {
            logger.info("Login attempt for email: {}", loginRequest.getEmail());

            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Load user details and generate token
            UserDetails userDetails = userService.loadUserByUsername(loginRequest.getEmail());
            String token = jwtTokenUtil.generateToken(userDetails);

            // Set secure, HttpOnly cookie
            setJwtCookie(response, token);

            // Prepare response
            LoginResponse loginResponse = new LoginResponse(
                    "Login successful",
                    token,
                    userDetails.getUsername(),
                    userDetails.getAuthorities()
            );

            logger.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(loginResponse);

        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Authentication failed", "Invalid credentials"));
        } catch (Exception e) {
            logger.error("Login error for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed", e.getMessage()));
        }
    }

    /**
     * Sets JWT token in a secure, HttpOnly cookie.
     *
     * @param response HttpServletResponse
     * @param token JWT token
     */
    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days expiration
        response.addCookie(cookie);
    }

    /**
     * Data transfer object for login requests.
     */
    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Data transfer object for successful login responses.
     */
    public static class LoginResponse {
        private String message;
        private String token;
        private String username;
        private Object authorities;

        public LoginResponse(String message, String token, String username, Object authorities) {
            this.message = message;
            this.token = token;
            this.username = username;
            this.authorities = authorities;
        }

        // Getters
        public String getMessage() {
            return message;
        }

        public String getToken() {
            return token;
        }

        public String getUsername() {
            return username;
        }

        public Object getAuthorities() {
            return authorities;
        }
    }

    /**
     * Data transfer object for error responses.
     */
    public static class ErrorResponse {
        private String error;
        private String details;

        public ErrorResponse(String error, String details) {
            this.error = error;
            this.details = details;
        }

        // Getters
        public String getError() {
            return error;
        }

        public String getDetails() {
            return details;
        }
    }
}