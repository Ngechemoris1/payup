package payup.payup.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Represents a rent payment in the PayUp system. This class is an entity 
 * mapped to the "rents" table in the database.
 */
@Entity
@Table(name = "rents")
public class Rent {
    
    /**
     * The unique identifier for the rent record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The amount of rent due.
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * The due date for the rent payment.
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Indicates whether the rent has been paid.
     */
    @Column(name = "is_paid", nullable = false)
    private boolean paid;

    /**
     * The tenant associated with this rent payment. 
     * This establishes a many-to-one relationship with the Tenant entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * Additional notes or comments about the rent payment.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Constructors

    /**
     * Default constructor for JPA and other frameworks.
     */
    public Rent() {
    }

    /**
     * Constructor to create a new rent record with the necessary details.
     * 
     * @param amount The amount of rent due.
     * @param dueDate The date by which the rent is due.
     * @param tenant The tenant who owes the rent.
     */
    public Rent(BigDecimal amount, LocalDate dueDate, Tenant tenant) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.tenant = tenant;
        this.paid = false; // Default to unpaid
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Custom methods

    /**
     * Marks the rent as paid.
     */
    public void markAsPaid() {
        this.paid = true;
    }

    /**
     * Checks if the rent is overdue based on the current date.
     * 
     * @return true if the rent is overdue, false otherwise.
     */
    public boolean isOverdue() {
        return !this.paid && LocalDate.now().isAfter(this.dueDate);
    }

    // Override toString for better logging and debugging
    @Override
    public String toString() {
        return "Rent{" +
               "id=" + id +
               ", amount=" + amount +
               ", dueDate=" + dueDate +
               ", paid=" + paid +
               ", tenant=" + (tenant != null ? tenant.getId() : "null") +
               ", notes='" + notes + '\'' +
               '}';
    }
}