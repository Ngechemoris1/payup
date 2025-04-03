package payup.payup.dto;

import lombok.Data;

import java.util.Map;

@Data
public class MpesaCallbackDto {
    private String transactionId;
    private Double amount;
    private String phoneNumber;
    private String status;
    private Map<String, Object> rawData;
}