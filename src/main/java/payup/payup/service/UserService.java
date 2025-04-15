package payup.payup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payup.payup.exception.DuplicateEmailException;
import payup.payup.exception.UserNotFoundException;
import payup.payup.model.User;
import payup.repository.UserRepository;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(@NotNull User user) {
        validateUser(user);
        logger.info("Registering user: email={}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Duplicate email detected: {}", user.getEmail());
            throw new DuplicateEmailException("Email is already registered: " + user.getEmail());
        }

        user.setPassword(encoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        logger.debug("User registered: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Transactional
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(@NotNull User user) throws UserNotFoundException {
        logger.info("Updating user: id={}", user.getId());
        User existingUser = findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + user.getId()));
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(encoder.encode(user.getPassword()));
        }
        User updatedUser = userRepository.save(existingUser);
        logger.debug("User updated: id={}", updatedUser.getId());
        return updatedUser;
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) throws UserNotFoundException {
        logger.info("Deleting user: id={}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
        logger.debug("User deleted: id={}", userId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "allUsers", key = "#pageable")
    public Page<User> findAll(Pageable pageable) {
        logger.info("Retrieving all users with pagination");
        Page<User> users = userRepository.findAll(pageable);
        logger.debug("Retrieved {} users", users.getTotalElements());
        return users;
    }

    @Transactional(readOnly = true)
    @Cacheable("users")
    public Optional<User> findById(Long id) {
        logger.info("Finding user: id={}", id);
        Optional<User> user = userRepository.findById(id);
        logger.debug("User found: id={}, present={}", id, user.isPresent());
        return user;
    }

    @Transactional(readOnly = true)
    @Cacheable("usersByEmail")
    public Optional<User> findByEmail(String email) {
        logger.info("Finding user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        logger.debug("User found: email={}, present={}", email, user.isPresent());
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findAdmin() {
        logger.info("Finding admin user");
        Optional<User> admin = userRepository.findFirstByRole(User.UserRole.ADMIN);
        logger.debug("Admin found: present={}", admin.isPresent());
        return admin;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "usersByRole", key = "#role + #pageable")
    public Page<User> findByRole(User.UserRole role, Pageable pageable) {
        logger.info("Retrieving users by role: {}", role);
        Page<User> users = userRepository.findAllByRole(role, pageable);
        logger.debug("Retrieved {} users with role {}", users.getTotalElements(), role);
        return users;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "usersBySearch", key = "#searchTerm + #pageable")
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        logger.info("Searching users with term: {}", searchTerm);
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        logger.debug("Retrieved {} users matching search term", users.getTotalElements());
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username: {}", username);
        return userRepository.findByEmail(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                ).orElseThrow(() -> {
                    logger.error("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }

    @Transactional(readOnly = true)
    public User getCurrentAdminUser() {
        logger.info("Retrieving current admin user");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            logger.error("No authenticated user found");
            throw new RuntimeException("No authenticated user");
        }
        String email = auth.getName();
        User admin = findByEmail(email)
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .orElseThrow(() -> {
                    logger.error("Current user is not an admin: email={}", email);
                    return new RuntimeException("Current user is not an admin");
                });
        logger.debug("Current admin retrieved: id={}", admin.getId());
        return admin;
    }

    @Transactional(readOnly = true)
    public User getAuthenticatedUser() {
        logger.info("Retrieving authenticated user");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            logger.error("No authenticated user found");
            throw new RuntimeException("No authenticated user");
        }
        String email = auth.getName();
        User user = findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found for email: {}", email);
                    return new RuntimeException("User not found");
                });
        logger.debug("Authenticated user retrieved: id={}, role={}", user.getId(), user.getRole());
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getPassword() == null || user.getRole() == null) {
            logger.error("Invalid user data: {}", user);
            throw new IllegalArgumentException("Email, password, and role are required");
        }
    }
}