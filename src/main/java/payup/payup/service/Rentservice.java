package payup.payup.service;

import org.springframework.transaction.annotation.Transactional;
import payup.payup.model.Rent;
import payup.repository.RentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;

@Service
public class RentService {

    private static final Logger logger = LoggerFactory.getLogger(RentService.class);

    @Autowired
    private RentRepository rentRepository;
    
    @Transactional(readOnly = true)

    /**
     * Retrieves all rent records for a specific tenant.
     * 
     * @param tenantId The ID of the tenant whose rent records are needed.
     * @return A list of Rent objects for the tenant.
     */
    public List<Rent> getRentsByTenant(Long tenantId) {
        List<Rent> rents = rentRepository.findByTenantId(tenantId);
        logger.info("Retrieved {} rent records for tenant ID: {}", rents.size(), tenantId);
        return rents;
    }

    /**
     * Adds a new rent record for a tenant.*/
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
        logger.info("Marking rent as paid: id={}", rentId);
        return rentRepository.findById(rentId).map(rent -> {
            rent.markAsPaid();
            rentRepository.save(rent);
            logger.debug("Rent marked as paid: id={}", rentId);
            return true;
        }).orElseGet(() -> {
            logger.warn("Rent not found: id={}", rentId);
            return false;
        });
    }
}