package payup.payup.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import payup.payup.model.Payment;
import payup.payup.model.Tenant;
import payup.repository.BillRepository;
import payup.payup.model.Bill;
import payup.repository.PaymentRepository;
import payup.repository.TenantRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository; // Injects PaymentRepository for payment operations

    @Autowired
    private TenantRepository tenantRepository; // Injects TenantRepository to update tenant balance

    @Autowired
    private BillRepository billRepository; // Injects BillRepository to link payments to bills

    /**
     * Processes a payment by saving it to the database.
     *
     * @param payment The Payment object to process, including tenant association.
     * @return The saved Payment entity after persistence.
     * @throws IllegalArgumentException if payment details are invalid.
     */
    @Transactional
    public Payment processPayment(Long tenantId, Payment payment) {
        if (payment == null || payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0 ||
                payment.getPaymentMethod() == null) {
            logger.error("Invalid payment details: {}", payment);
            throw new IllegalArgumentException("Payment amount and method must be valid");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    logger.error("Tenant not found: tenantId={}", tenantId);
                    return new RuntimeException("Tenant not found with ID: " + tenantId);
                });

        payment.setTenant(tenant);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(Payment.Status.PENDING);

        if (payment.getBill() != null) {
            Bill bill = billRepository.findById(payment.getBill().getId())
                    .orElseThrow(() -> {
                        logger.error("Bill not found: billId={}", payment.getBill().getId());
                        return new RuntimeException("Bill not found");
                    });
            payment.setBill(bill);
        }

        Payment savedPayment = paymentRepository.save(payment);
        updateTenantBalance(tenant, savedPayment);
        logger.info("Payment processed: paymentId={}, tenantId={}", savedPayment.getId(), tenantId);
        return savedPayment;
    }
    // Retrieves total payments for a tenant
    @Transactional(readOnly = true)
    public BigDecimal getTotalPayments(Long tenantId) {
        if (tenantId == null) {
            logger.error("Tenant ID cannot be null");
            throw new IllegalArgumentException("Tenant ID must not be null");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    logger.error("Tenant not found: tenantId={}", tenantId);
                    return new RuntimeException("Tenant not found with ID: " + tenantId);
                });

        BigDecimal total = paymentRepository.sumPaymentsByTenant(tenant) != null
                ? BigDecimal.valueOf(paymentRepository.sumPaymentsByTenant(tenant))
                : BigDecimal.ZERO;
        BigDecimal result = total != null ? total : BigDecimal.ZERO;
        logger.debug("Total payments for tenantId={}: {}", tenantId, result);
        return result;
    }

    private void updateTenantBalance(Tenant tenant, Payment payment) {
        if (payment.getStatus() == Payment.Status.PAID) {
            BigDecimal newBalance = BigDecimal.valueOf(tenant.getBalance()).subtract(payment.getAmount());
            tenant.setBalance(newBalance.doubleValue());
            tenantRepository.save(tenant);
            logger.debug("Updated tenant balance: tenantId={}, newBalance={}", tenant.getId(), newBalance);
        }
    }

    @Transactional
    public void completePayment(Long paymentId, String mpesaReceiptNumber) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    logger.error("Payment not found: paymentId={}", paymentId);
                    return new RuntimeException("Payment not found with ID: " + paymentId);
                });

        payment.PAIDPayment(mpesaReceiptNumber);
        paymentRepository.save(payment);
        updateTenantBalance(payment.getTenant(), payment);
        logger.info("Payment completed: paymentId={}, receipt={}", paymentId, mpesaReceiptNumber);
    }
}
