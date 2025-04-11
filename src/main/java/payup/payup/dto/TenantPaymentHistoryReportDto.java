package payup.payup.dto;

import lombok.Data;
import payup.payup.dto.PaymentDto;

import java.util.List;

/**
 * DTO representing a tenant payment history report.
 */
@Data
public class TenantPaymentHistoryReportDto {
    private Long tenantId;
    private List<PaymentDto> payments;

    public TenantPaymentHistoryReportDto(Long tenantId, List<PaymentDto> payments) {
        this.tenantId = tenantId;
        this.payments = payments;
    }
}
