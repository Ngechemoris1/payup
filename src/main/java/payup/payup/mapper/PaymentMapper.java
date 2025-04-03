package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.PaymentDto;
import payup.payup.model.Payment;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setStatus(payment.getStatus().name());
        dto.setPaidAt(payment.getPaidAt());
        dto.setMpesaReceiptNumber(payment.getMpesaReceiptNumber());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setIdempotencyKey(payment.getIdempotencyKey());

        if (payment.getTenant() != null) {
            dto.setTenantId(payment.getTenant().getId());
        }
        if (payment.getBill() != null) {
            dto.setBillId(payment.getBill().getId());
        }
        return dto;
    }

    public Payment toEntity(PaymentDto dto) {
        if (dto == null) {
            return null;
        }

        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setAmount(dto.getAmount());
        if (dto.getPaymentMethod() != null) {
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(dto.getPaymentMethod()));
        }
        payment.setTransactionId(dto.getTransactionId());
        payment.setPaymentDate(dto.getPaymentDate());
        if (dto.getStatus() != null) {
            payment.setStatus(Payment.Status.valueOf(dto.getStatus()));
        }
        payment.setPaidAt(dto.getPaidAt());
        payment.setMpesaReceiptNumber(dto.getMpesaReceiptNumber());
        payment.setPaymentReference(dto.getPaymentReference());
        payment.setIdempotencyKey(dto.getIdempotencyKey());
        // Relationships should be set separately via service
        return payment;
    }
}