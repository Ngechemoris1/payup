package payup.payup.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a bill in the property management system, associated with a tenant.
 * Tracks bill details such as type, amount, due date, and payment status.
 */
@Data
@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String billType; // e.g., RENT, SECURITY, GARBAGE, WATER, ELECTRICITY

    @Column(nullable = false)
    @Positive(message = "Amount must be positive")
    private double amount;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private boolean isPaid;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status;

    public Bill() {

    }

    /**
     * Enum representing the possible statuses of a bill.
     */
    public enum BillStatus {
        PENDING, PAID, OVERDUE
    }

    public boolean isOverdue() {
        return this.getDueDate() != null && this.getDueDate().isBefore(java.time.LocalDate.now().atStartOfDay());
    }

    public Bill(String billType, double amount, LocalDateTime dueDate, Tenant tenant) {
        this.billType = billType;
        this.amount = amount;
        this.dueDate = dueDate;
        this.tenant = tenant;
        this.isPaid = false;
        this.status = BillStatus.PENDING;
    }
}