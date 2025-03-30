package payup.payup.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

/**
 * Represents a bill for a landlord, typically for property-related expenses.
 */
@Entity
@Table(name = "landlord_bills")
@Data
public class LandlordBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the bill

    @ManyToOne
    @JoinColumn(name = "landlord_id", nullable = false)
    private User landlord; // Landlord associated with the bill

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property; // Property associated with the bill

    @Column(nullable = false)
    private String type; // Type of bill (e.g., Maintenance, Utilities)

    @Column(nullable = false)
    @Positive(message = "Amount must be positive")
    private Double amount; // Amount due for the bill

    @Column(name = "due_date", nullable = false )
    private LocalDate dueDate; // Due date for the bill
}