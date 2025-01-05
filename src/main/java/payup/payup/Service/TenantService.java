package payup.payup.service;

import payup.payup.model.Tenant;
import payup.payup.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Saves a new tenant or updates an existing one.
     * 
     * @param tenant The tenant to save or update.
     * @return The saved or updated tenant object.
     */
    @Transactional
    public Tenant save(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    /**
     * Finds a tenant by their ID.
     * 
     * @param id The ID of the tenant to find.
     * @return The tenant if found, null otherwise.
     */
    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves all tenants for a specific property.
     * 
     * @param propertyId The ID of the property to fetch tenants for.
     * @return A list of tenants in that property.
     */
    public List<Tenant> getTenantsByProperty(Long propertyId) {
        return tenantRepository.findByPropertyId(propertyId);
    }

    /**
     * Finds a tenant by their email address.
     * 
     * @param email The email address to search for.
     * @return The tenant if found, null otherwise.
     */
    public Tenant findByEmail(String email) {
        return tenantRepository.findByEmail(email).orElse(null);
    }

    /**
     * Checks if a tenant with the given ID belongs to a property owned by the specified landlord.
     * 
     * @param tenantId The ID of the tenant to check.
     * @param propertyOwnerId The ID of the property owner to verify against.
     * @return true if the tenant belongs to a property owned by the landlord, false otherwise.
     */
    public boolean isTenantOfLandlord(Long tenantId, Long propertyOwnerId) {
        return tenantRepository.existsByIdAndPropertyOwnerId(tenantId, propertyOwnerId);
    }
}