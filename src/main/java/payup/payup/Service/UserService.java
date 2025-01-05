package payup.payup.service;

import payup.payup.exception.UserNotFoundException;
import payup.payup.model.User;
import payup.payup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Registers a new user in the system.
     * @param user The user to register including details like email, name, phone, password, and role.
     * @return The saved user object with the generated ID.
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's information.
     * @param user The user object with updated information.
     * @return The updated user object after saving changes to the database.
     * @throws UserNotFoundException if the user with the given ID does not exist.
     */
    @Transactional
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) throws UserNotFoundException {
        User existingUser = findById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.encodePassword(user.getPassword());
        }
        return userRepository.save(existingUser);
    }

    /**
     * Deletes a user from the system by ID.
     * @param userId The ID of the user to delete.
     * @throws UserNotFoundException if no user with the given ID exists.
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) throws UserNotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    /**
     * Retrieves all users in the system.
     * @return A list of all users.
     */
    @Cacheable("allUsers")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by their ID.
     * @param id The ID of the user to find.
     * @return An Optional containing the User if found, empty otherwise.
     */
    @Cacheable("users")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their email address.
     * @param email The email address to search for.
     * @return An Optional containing the User if found, empty otherwise.
     */
    @Cacheable("usersByEmail")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds the first admin user in the system.
     * @return An Optional containing the admin User if found, empty if no admin exists.
     */
    public Optional<User> findAdmin() {
        return userRepository.findFirstByRole(User.UserRole.ADMIN);
    }

    /**
     * Loads user details by username for Spring Security authentication.
     * @param username The email used as the username for authentication.
     * @return UserDetails for the authenticated user.
     * @throws UsernameNotFoundException if no user is found with the given username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(), 
                        user.getPassword(), 
                        org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * Helper method to encode a user's password.
     * @param user The user whose password needs to be encoded.
     */
    private void encodePassword(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
    }
}