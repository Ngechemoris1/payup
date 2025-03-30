package payup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import payup.payup.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findFirstByRole(User.UserRole role);

    Page<User> findAllByRole(User.UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}