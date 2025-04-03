package payup.payup.dto;

import lombok.Data;

@Data
public class PaymentResponseDto {
    private String message;
    private String checkoutRequestId;
    private Double amount;
    private String phoneNumber;
    private Long tenantId;
    private Long billId;
}
