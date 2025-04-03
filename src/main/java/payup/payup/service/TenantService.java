package payup.payup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import payup.payup.model.Tenant;
import payup.repository.TenantRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing tenant-related operations in the PayUp system.
 * Provides methods for retrieving, saving, updating, and deleting tenants, integrating
 * business logic with the TenantRepository for persistence.
 */
@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Retrieves a tenant by their email address.
     *
     * @param email The email address of the tenant.
     * @return The Tenant object if found, or null if not found.
     */
    public Tenant findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Attempted to find tenant with null or empty email");
            return null;
        }

        logger.debug("Searching for tenant by email: {}", email);
        Optional<Tenant> tenantOptional = tenantRepository.findByEmail(email);
        if (tenantOptional.isPresent()) {
            logger.debug("Tenant found by email: id={}", tenantOptional.get().getId());
            return tenantOptional.get();
        } else {
            logger.debug("No tenant found for email: {}", email);
            return null;
        }
    }

    /**
     * Retrieves a tenant by their ID.
     *
     * @param id The ID of the tenant.
     * @return The Tenant object if found, or null if not found.
     */
    public Tenant getTenantById(Long id) {
        if (id == null || id <= 0) {
            logger.warn("Invalid tenant ID provided: {}", id);
            return null;
        }

        logger.debug("Searching for tenant by ID: {}", id);
        Optional<Tenant> tenantOptional = tenantRepository.findById(id);
        if (tenantOptional.isPresent()) {
            logger.debug("Tenant found by ID: {}", id);
            return tenantOptional.get();
        } else {
            logger.debug("No tenant found for ID: {}", id);
            return null;
        }
    }

    /**
     * Saves or updates a tenant in the system.
     *
     * @param tenant The Tenant object to save or update.
     * @return The saved or updated Tenant object.
     * @throws IllegalArgumentException if the tenant data is invalid (e.g., null or missing required fields).
     */
    public Tenant save(Tenant tenant) {

        if (tenant == null) {
            logger.error("Attempted to save null tenant");
            throw new IllegalArgumentException("Tenant cannot be null");
        }
        if (tenant.getEmail() == null || tenant.getEmail().trim().isEmpty()) {
            logger.error("Attempted to save tenant with null or empty email");
            throw new IllegalArgumentException("Tenant email cannot be null or empty");
        }
        if (tenant.getName() == null || tenant.getName().trim().isEmpty()) {
            logger.error("Attempted to save tenant with null or empty name");
            throw new IllegalArgumentException("Tenant name cannot be null or empty");
        }

        // Check for duplicate email if it's a new tenant or email is being changed
        Tenant existingTenant = tenant.getId() == null ? null : getTenantById(tenant.getId());
        if (existingTenant == null || !existingTenant.getEmail().equals(tenant.getEmail())) {
            Optional<Tenant> duplicateCheck = tenantRepository.findByEmail(tenant.getEmail());
            if (duplicateCheck.isPresent()) {
                logger.error("Email {} is already in use by another tenant", tenant.getEmail());
                throw new IllegalArgumentException("Email is already in use");
            }
        }

        logger.info("Saving tenant: email={}", tenant.getEmail());
        Tenant savedTenant = tenantRepository.save(tenant);
        logger.debug("Tenant saved successfully: id={}", savedTenant.getId());
        return savedTenant;
    }

    /**
     * Deletes a tenant by their ID.
     *
     * @param id The ID of the tenant to delete.
     * @throws IllegalArgumentException if the tenant does not exist or ID is invalid.
     */
    public void delete(Long id) {
        if (id == null || id <= 0) {
            logger.error("Invalid tenant ID for deletion: {}", id);
            throw new IllegalArgumentException("Invalid tenant ID");
        }

        if (!tenantRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent tenant: id={}", id);
            throw new IllegalArgumentException("Tenant not found with ID: " + id);
        }

        logger.info("Deleting tenant: id={}", id);
        tenantRepository.deleteById(id);
        logger.debug("Tenant deleted successfully: id={}", id);
    }

    /**
     * Retrieves all tenants in the system.
     *
     * @return A list of all Tenant objects.
     */
    public List<Tenant> findAll() {
        logger.info("Fetching all tenants");
        List<Tenant> tenants = tenantRepository.findAll();
        logger.debug("Retrieved {} tenants", tenants.size());
        return tenants;
    }

    /**
     * Retrieves all tenants associated with a specific property.
     *
     * @param propertyId The ID of the property.
     * @return A list of Tenant objects associated with the property.
     * @throws IllegalArgumentException if the property ID is invalid.
     */
    public List<Tenant> getTenantsByProperty(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            logger.error("Invalid property ID provided: {}", propertyId);
            throw new IllegalArgumentException("Invalid property ID");
        }

        logger.info("Fetching tenants for property: id={}", propertyId);
        List<Tenant> tenants = tenantRepository.findByPropertyId(propertyId);
        logger.debug("Retrieved {} tenants for property: id={}", tenants.size(), propertyId);
        return tenants;
    }
}