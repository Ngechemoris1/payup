package payup.payup.model;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import java.time.LocalDateTime;

/**
 * Entity representing a payment record in the system, linked to a tenant and optionally a bill.
 * Tracks payment details including amount, method, and status.
 */
@Entity
@Data
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill; // Nullable for partial payments not tied to a specific bill

    @NotNull
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; 

    @Column(name = "paid_at")
    private LocalDateTime paidAt; 
    
    @Column(name = "mpesa_receipt_number")
    private String mpesaReceiptNumber; //  M-Pesa receipt

    @Column
    private String paymentReference;

    /**
     * Enum representing supported payment methods.
     */
    public enum PaymentMethod {
        MPESA, FAMILY_BANK, EQUITY_BANK
    }

    /**
     * Enum representing the status of a payment.
     */
    public enum Status {
        PENDING, PAID, FAILED
    }

    // Manual getters and setters for mpesaReceiptNumber (since @Data might not suffice for all IDEs)
    public String getMpesaReceiptNumber() {
        return mpesaReceiptNumber;
    }

    /**
     * Marks the payment as PAID and sets the Mpesa receipt number.
     *
     * @param mpesaReceiptNumber The Mpesa receipt number for the payment.
     */
    public void PAIDPayment(String mpesaReceiptNumber) {
        this.status = Status.PAID; // Assuming 'status' is a field in Payment
        this.mpesaReceiptNumber = mpesaReceiptNumber; // Assuming 'mpesaReceiptNumber' is a field in Payment
    }

    public void setMpesaReceiptNumber(String mpesaReceiptNumber) {
        this.mpesaReceiptNumber = mpesaReceiptNumber;
    }
    
        private String idempotencyKey;
    
        public String getIdempotencyKey() {
            return idempotencyKey;
        }
    
        public void setIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
        }
}