package payup.payup.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import payup.payup.dto.*;
import payup.payup.exception.DuplicateEmailException;
import payup.payup.jwt.JwtTokenUtil;
import payup.payup.mapper.UserMapper;
import payup.payup.model.User;
import payup.payup.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST controller for handling authentication operations including
 * user registration and login. Uses JWT for authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    /**
     * Constructs an AuthController with required dependencies.
     *
     * @param userService The service for user operations.
     * @param jwtTokenUtil Utility for JWT token generation and validation.
     * @param authenticationManager Spring Security authentication manager.
     * @param userMapper Mapper for converting between User entities and DTOs.
     */
    @Autowired
    public AuthController(UserService userService,
                          JwtTokenUtil jwtTokenUtil,
                          AuthenticationManager authenticationManager,
                          UserMapper userMapper) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    /**
     * Registers a new user in the system.
     *
     * @param userDto User registration data.
     * @return ResponseEntity with the registered UserDto or an error response.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserDto userDto) {
        logger.info("Attempting to register user with email: {}", userDto.getEmail());
        try {
            User user = userMapper.toEntity(userDto);
            User registeredUser = userService.registerUser(user);
            UserDto responseDto = userMapper.toDto(registeredUser);
            logger.info("User registered successfully: {}", userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (DuplicateEmailException e) {
            logger.warn("Registration failed for email {}: {}", userDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Email already registered", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed for email {}: {}", userDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid registration data", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during registration for email {}: {}", userDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Registration failed", "An unexpected error occurred"));
        }
    }

    /**
     * Authenticates a user and issues a JWT token upon successful login.
     *
     * @param loginRequest User credentials (email and password).
     * @param response HttpServletResponse for setting secure cookies.
     * @return ResponseEntity with authentication details or an error response.
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

            // Fetch the full User entity to get id and name
            User user = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            // Set secure, HttpOnly cookie
            setJwtCookie(response, token);

            // Prepare response with id and name
            LoginResponse loginResponse = new LoginResponse(
                    "Login successful",
                    token,
                    userDetails.getUsername(),
                    user.getId(),
                    user.getName(),
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
     * @param response HttpServletResponse to add the cookie to.
     * @param token JWT token to set.
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

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Data transfer object for successful login responses.
     */
    public static class LoginResponse {
        private String message;
        private String token;
        private String username;
        private Long id;         // Added for user ID
        private String name;     // Added for user name
        private Object authorities;

        public LoginResponse(String message, String token, String username, Long id, String name, Object authorities) {
            this.message = message;
            this.token = token;
            this.username = username;
            this.id = id;
            this.name = name;
            this.authorities = authorities;
        }

        public String getMessage() { return message; }
        public String getToken() { return token; }
        public String getUsername() { return username; }
        public Long getId() { return id; }
        public String getName() { return name; }
        public Object getAuthorities() { return authorities; }
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

        public String getError() { return error; }
        public String getDetails() { return details; }
    }
}