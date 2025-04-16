package payup.payup.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a bill in the system.
 */
@Entity
@Table(name = "bill")
public class Bill {

    public enum BillStatus { PENDING, PAID, OVERDUE }

    // Getters and setters
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private String billType;

    @Setter
    @Getter
    private double amount;

    @Getter
    @Setter
    private LocalDateTime dueDate;

    private boolean isPaid;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private BillStatus status;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    @JsonBackReference(value = "user-bills")
    private User tenant;

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public boolean isOverdue() {
        return !isPaid && dueDate != null && dueDate.isBefore(LocalDateTime.now());
    }
}