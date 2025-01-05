package payup.payup.service;

import payup.payup.model.Rent;
import payup.payup.repository.RentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentService {

    @Autowired
    private RentRepository rentRepository;

    /**
     * Retrieves all rent records for a specific tenant.
     * 
     * @param tenantId The ID of the tenant whose rent records are needed.
     * @return A list of Rent objects for the tenant.
     */
    public List<Rent> getRentsByTenant(Long tenantId) {
        return rentRepository.findByTenantId(tenantId);
    }

    /**
     * Adds a new rent record for a tenant.
     * 
     * @param rent The rent object to save.
     * @return The saved rent object.
     */
    @Transactional
    public Rent saveRent(Rent rent) {
        return rentRepository.save(rent);
    }

    /**
     * Marks a rent payment as paid.
     * 
     * @param rentId The ID of the rent payment to mark as paid.
     * @return true if the rent was found and marked as paid, false otherwise.
     */
    @Transactional
    public boolean markRentAsPaid(Long rentId) {
        return rentRepository.findById(rentId).map(rent -> {
            rent.markAsPaid();
            rentRepository.save(rent);
            return true;
        }).orElse(false);
    }
}