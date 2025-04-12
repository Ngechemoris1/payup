package payup.payup.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import payup.payup.model.Bill;

/**
 * Data Transfer Object for Bill entity.
 */
public class BillDto {

    private Long id;

    @NotNull(message = "Bill type is required")
    private String billType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private double amount;

    @NotNull(message = "Due date is required")
    private String dueDate;

    private boolean isPaid;

    private String status;

    private boolean overdue;

    // Default constructor
    public BillDto() {
    }

    // Constructor that accepts Bill entity
    public BillDto(Bill bill) {
        this.id = bill.getId();
        this.billType = bill.getBillType();
        this.amount = bill.getAmount();
        this.dueDate = bill.getDueDate() != null ? bill.getDueDate().toString() : null;
        this.isPaid = bill.isPaid();
        this.status = bill.getStatus() != null ? bill.getStatus().name() : null;
        this.overdue = bill.isOverdue();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}