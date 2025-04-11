package payup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import payup.payup.model.User;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides methods for finding, searching, and checking existence of users in the database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return An Optional containing the User if found, or empty if not.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email The email address to check.
     * @return true if a user with the email exists, false otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Finds the first user with the specified role.
     *
     * @param role The role to search for.
     * @return An Optional containing the first User with the role if found, or empty if not.
     */
    Optional<User> findFirstByRole(User.UserRole role);

    /**
     * Retrieves all users with the specified role, with pagination.
     *
     * @param role The role to filter by.
     * @param pageable Pagination and sorting parameters.
     * @return A Page of User entities with the specified role.
     */
    Page<User> findAllByRole(User.UserRole role, Pageable pageable);

    /**
     * Searches users by a term across firstName, lastName, email, or phone, with pagination.
     *
     * @param searchTerm The term to search for (case-insensitive).
     * @param pageable Pagination and sorting parameters.
     * @return A Page of User entities matching the search term.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}