package payup.payup.repository;

import payup.payup.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on User entities.
 * This interface extends JpaRepository to provide basic CRUD functionality 
 * and includes custom query methods for more specific operations.
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
     * Finds the first user with the specified role.
     * Useful for retrieving an admin user, for instance.
     * 
     * @param role The role to search for.
     * @return An Optional containing the first User with the specified role, or empty if no such user exists.
     */
    Optional<User> findFirstByRole(User.UserRole role);

    /**
     * Finds users by role with pagination support.
     * 
     * @param role The role to filter users by.
     * @param pageable Pagination information.
     * @return A Page of users with the specified role.
     */
    Page<User> findAllByRole(User.UserRole role, Pageable pageable);

    /**
     * Searches for users based on partial matches of name, email, or phone.
     * 
     * @param searchTerm A string to search within user's name, email, or phone.
     * @return A list of users matching the search term.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
            + "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}