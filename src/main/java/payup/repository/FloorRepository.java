package payup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payup.payup.model.Floor;

/**
 * Repository interface for managing Floor entities in the PayUp system.
 * Extends JpaRepository to provide basic CRUD operations.
 */
public interface FloorRepository extends JpaRepository<Floor, Long> {
}