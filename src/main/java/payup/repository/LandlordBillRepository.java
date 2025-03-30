package payup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import payup.payup.model.LandlordBill;
import payup.payup.model.Property;
import payup.payup.model.User;

import java.util.List;

/**
 * Repository interface for managing LandlordBill entities in the PayUp system.
 * Provides CRUD operations and custom queries for retrieving landlord bills based on
 * landlord and property associations.
 */
@Repository
public interface LandlordBillRepository extends JpaRepository<LandlordBill, Long> {

    /**
     * Retrieves all landlord bills associated with a specific landlord.
     *
     * @param landlord The User entity representing the landlord.
     * @return A list of LandlordBill entities linked to the specified landlord.
     */
    List<LandlordBill> findByLandlord(User landlord);

    /**
     * Retrieves all landlord bills associated with a specific property.
     *
     * @param property The Property entity to query by.
     * @return A list of LandlordBill entities linked to the specified property.
     */
    List<LandlordBill> findByProperty(Property property);

    /**
     * Retrieves all landlord bills associated with both a specific landlord and property.
     *
     * @param landlord The User entity representing the landlord.
     * @param property The Property entity to query by.
     * @return A list of LandlordBill entities linked to both the landlord and property.
     */
    List<LandlordBill> findByLandlordAndProperty(User landlord, Property property);

    /**
     * Retrieves all landlord bills by landlord ID and property ID.
     *
     * @param landlordId The ID of the landlord.
     * @param propertyId The ID of the property.
     * @return A list of LandlordBill entities matching the specified landlord and property IDs.
     */
    @Query("SELECT lb FROM LandlordBill lb WHERE lb.landlord.id = :landlordId AND lb.property.id = :propertyId")
    List<LandlordBill> findByLandlordIdAndPropertyId(Long landlordId, Long propertyId);
}