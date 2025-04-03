package payup.payup.dto;

import payup.payup.model.Rent;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RentDto {
    private Long id;
    private BigDecimal amount;
    private LocalDate dueDate;
    private boolean paid;
    private Long tenantId;
    private String notes;
    private boolean overdue;

    // Default constructor
    public RentDto() {
    }

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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    // Constructor that accepts Rent entity
    public RentDto(Rent rent) {
        this.id = rent.getId();
        this.amount = rent.getAmount();
        this.dueDate = rent.getDueDate();
        this.paid = rent.isPaid();
        this.notes = rent.getNotes();
        this.overdue = rent.isOverdue();
        if (rent.getTenant() != null) {
            this.tenantId = rent.getTenant().getId();
        }
    }

}