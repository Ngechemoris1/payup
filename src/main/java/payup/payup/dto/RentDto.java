package payup.payup.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentDto {
    private Long id;
    private Long tenantId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private boolean paid;
    private String notes;
    private boolean overdue;

    @JsonBackReference
    private TenantDto tenant;
}