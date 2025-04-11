package payup.payup.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import payup.payup.model.Bill;
import java.time.LocalDateTime;

public class BillDto {
    private Long id;
    private String billType;
    private double amount;
    private LocalDateTime dueDate;
    private boolean isPaid;
    private Long tenantId;
    private String status;
    private boolean overdue;

    @JsonBackReference // Prevents recursion back to tenant
    private TenantDto tenant;

    // Default constructor
    public BillDto() {
    }

    // Constructor that accepts Bill entity
    public BillDto(Bill bill) {
        this.id = bill.getId();
        this.billType = bill.getBillType();
        this.amount = bill.getAmount();
        this.dueDate = bill.getDueDate();
        this.isPaid = bill.isPaid();
        this.status = bill.getStatus().name();
        this.overdue = bill.isOverdue();
        if (bill.getTenant() != null) {
            this.tenantId = bill.getTenant().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    // Add getter and setter for tenant
    public TenantDto getTenant() {
        return tenant;
    }

    public void setTenant(TenantDto tenant) {
        this.tenant = tenant;
    }
}